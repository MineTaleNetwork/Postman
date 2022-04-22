package cc.minetale.postman.payload;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class ListenerRegistry {

    @Getter private final Set<PayloadListener> listeners = new HashSet<>();

    public void callListeners(Payload payload) {
        try {
            for(var listener : listeners) {
                var listenerClass = listener.getClass();
                for(var method : listenerClass.getDeclaredMethods()) {
                    if(!isHandler(method) || !isHandlerApplicable(method, payload)) { continue; }
                    method.invoke(listener, payload);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public boolean registerListener(PayloadListener listener) { return isListener(listener) && listeners.add(listener); }

    public static boolean isListener(Object listener) {
        return PayloadListener.class.isAssignableFrom(listener.getClass());
    }

    public static boolean isHandler(Method method) {
        return method.isAnnotationPresent(PayloadHandler.class) &&
                method.getParameterTypes().length == 1 &&
                Payload.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    public static boolean isHandlerApplicable(Method method, Payload payload) {
        return method.getParameterTypes()[0].isAssignableFrom(payload.getClass());
    }

}
