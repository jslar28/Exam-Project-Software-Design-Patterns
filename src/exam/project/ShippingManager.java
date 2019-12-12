package exam.project;

import java.util.ArrayList;

public class ShippingManager {
    private volatile static ShippingManager instance;

    private ArrayList<ISubscriber> subscribers = new ArrayList<>();
    public void subscribe(ISubscriber subscriber) {
        subscribers.add(subscriber);
    }
    public void unsubscribe(ISubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    private ShippingManager() {
        // Reflection-safe
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method instead.");
        }
    }

    public void notifySubscribers() {
        for (ISubscriber subscriber: subscribers ) {
            subscriber.update();
        }
    }

    public void ship() {
        notifySubscribers();
    }


    // Singleton proofing
    static ShippingManager getInstance() {
        // Lazy-initialization
        if (instance == null) {
            // Thread-safe
            synchronized (ShippingManager.class) {
                if (instance == null) {
                    instance = new ShippingManager();
                }
            }
        }
        return instance;
    }

    // Clone-safe
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Don't clone the singleton.");
    }

    // Serialization-safe
    protected Object readResolve() {
        return getInstance();
    }

}
