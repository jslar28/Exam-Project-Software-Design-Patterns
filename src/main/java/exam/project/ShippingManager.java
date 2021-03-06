package exam.project;

import exam.project.IShippingCareStrategy.IShippingCareStrategy;
import exam.project.IShippingTypeStrategy.IShippingTypeStrategy;
import exam.project.Products.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShippingManager implements IInventoryObserver, IOrderBookObserver {

    private volatile static ShippingManager instance;
    private ArrayList<ElectronicsProduct> currentInventory;
    private ArrayList<Order> currentOrders;

    private ShippingManager() {
        // Reflection-safe
        if (instance != null) {
            throw new RuntimeException("\nUse getInstance() method instead.\n");
        }
        Inventory.getInstance().addObserver(this);
        OrderBook.getInstance().addObserver(this);
        currentInventory = Inventory.getInstance().getElectronicsProducts();
        currentOrders = OrderBook.getInstance().getOrderBook();
    }

    public static ShippingManager getInstance() {
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

    @Override
    public void inventoryUpdate(Inventory inventory) {
        System.out.println("\nInventory updated - checking shippable orders.\n");
        currentInventory = inventory.getElectronicsProducts();
        checkForShippableOrders();
    }

    @Override
    public void orderBookUpdate(OrderBook orderBook) {
        System.out.println("\nOrder book updated - checking shippable orders.\n");
        currentOrders = orderBook.getOrderBook();
        checkForShippableOrders();
    }

    public void shipOrder(Order order) {
        IShippingTypeStrategy thisShippingTypeStrategy = order.getShippingTypeStrategy();
        IShippingCareStrategy thisShippingCareStrategy = order.getShippingCareStrategy();
        ShipItem shipItem = new ShipItem(thisShippingCareStrategy, thisShippingTypeStrategy);
        double totalCost = shipItem.calculateOrderCost(order);
        System.out.println("\nAn order has been shipped by " + thisShippingTypeStrategy.getClass().getSimpleName() +
                "\nand " + thisShippingCareStrategy.getClass().getSimpleName() + "!\n\nTotal cost for " +
                "shipping was: " + totalCost +
                "\n");
    }


    public void checkForShippableOrders() {
        System.out.println("\nInventory before: " + currentInventory.size());

        System.out.println("Inventory contains:");
        for (ElectronicsProduct product: currentInventory) {
            System.out.println(product.getClass().getSimpleName() + ": " + product.hashCode());
        }

        System.out.println("\nOrders before: " + currentOrders.size());

        if (currentOrders != null && currentOrders.size() > 0) {
            // Loop over each order in the order book
            ArrayList<Order> ordersToRemove = new ArrayList<>();
            for (int i = 0; i < currentOrders.size(); i++) {
                Order currentOrder = currentOrders.get(i);
                if (currentOrder.getItems() != null && currentOrder.getItems().size() > 0) {
                    // Create map to store needed quantities for each product type
                    Map<String, Integer> productQuantities = new HashMap<>();
                    for (int j = 0; j < currentOrder.getItems().size(); j++) {
                        ElectronicsProduct product = currentOrder.getItems().get(j);
                        String productName = product.getClass().getSimpleName();
                        if (!productQuantities.containsKey(productName)) {
                            productQuantities.put(productName, 1);
                        } else {
                            int currentQuantity = productQuantities.get(productName);
                            productQuantities.replace(productName, currentQuantity + 1);
                        }
                    }
                    int productNeeded = productQuantities.size();
                    int productsFound = 0;
                    ArrayList<ElectronicsProduct> electronicsProducts = new ArrayList<>();
                    // For each product in the order, get the quantity for each product needed for the order
                    for (String name : productQuantities.keySet()) {
                        int quantityNeeded = productQuantities.get(name);
                        int quantityFound = 0;

                        // For each product in the inventory, check if there's the required quantity needed in the order
                        for (int j = 0; j < currentInventory.size(); j++) {
                            ElectronicsProduct product = currentInventory.get(j);
                            // If the product needed is found, increment the quantities found, and add the
                            // ID of the product so it's easier to remove later on
                            // Also, only add it if we haven't already found the required amount
                            if (product.getClass().getSimpleName().equals(name) && quantityFound != quantityNeeded) {
                                electronicsProducts.add(product);
                                quantityFound += 1;
                            }
                        }
                        // If the quantities are found, the product is in stock in the right amount of quantity
                        // (But we still need to check the other products in the order)
                        if (quantityFound >= quantityNeeded) {
                            productsFound += 1;
                        } else {
                            // If the quantities aren't found, reset the IDs for deleted.
                            electronicsProducts = new ArrayList<>();
                        }
                    }

                    // If there's the required quantity for each product, the order can be completed.
                    // We need to remove the products from the inventory, to account for the next order (avoid double booking)
                    if (productsFound >= productNeeded) {
                        System.out.println("\nAn order could be shipped.\n");
                        shipOrder(currentOrder);
                        // Remove the current order from the order book, and all products for that order from the inventory
                        if (electronicsProducts.size() > 0) {
                            Inventory.getInstance().removeProducts(electronicsProducts);
                            ordersToRemove.add(currentOrder);
                            currentInventory = Inventory.getInstance().getElectronicsProducts();
                        }
                    } else {
                        // If there isn't the required quantity for each product,
                        // the order can't be fulfilled - move on to the next order
                        System.out.println("\nAn order could not be shipped due to lack of stock.\n");
                    }
                } else {
                    System.out.println("\nThere are no items in this order.\n");
                }
            }

            // When all orders have been looped through - delete the ones that were shipped.
            OrderBook.getInstance().removeOrders(ordersToRemove);
            currentOrders = OrderBook.getInstance().getOrderBook();

        } else {
            System.out.println("\nThere are no orders to check.\n");
        }

        System.out.println("\nInventory after: " + currentInventory.size());

        System.out.println("Inventory contains:");
        for (ElectronicsProduct product: currentInventory) {
            System.out.println(product.getClass().getSimpleName() + ": " + product.hashCode());
        }

        System.out.println("\nOrders after: " + currentOrders.size());
    }

    // Clone-safe
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("\nDon't clone the singleton.\n");
    }

    // Serialization-safe
    protected Object readResolve() {
        return getInstance();
    }
}