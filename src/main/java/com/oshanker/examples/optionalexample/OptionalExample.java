package com.oshanker.examples.optionalexample;

import java.util.Optional;

class ShippingAddress {
    private String city;

    public ShippingAddress(String city) { this.city = city; }
    public String getCity() { return city; }
}

class Customer {
    private ShippingAddress address; // Can be null
	private String name;

    @Override
	public String toString() {
		return "Customer [name=" + name + "]";
	}

	public Customer(String name, ShippingAddress address) { 
    	this.name = name;
    	this.address = address; 
    }
    
    // Natively returns an Optional because a customer might not have an address
    public Optional<ShippingAddress> getShippingAddress() {
        return Optional.ofNullable(address);
    }

	public String getName() {
		return name;
	}
}

class Order {
    private Customer customer; // Assume this won't be null for the example

    public Order(Customer customer) { this.customer = customer; }
    public Customer getCustomer() { return customer; } // Returns plain Customer
}


public class OptionalExample {
	public static Customer empty = new Customer("no address", null);
    public static void main(String[] args) {
        // Setup data: Order -> Customer -> ShippingAddress -> "Los Angeles"
        ShippingAddress address = new ShippingAddress("Los Angeles");
        Customer customer = new Customer("LA customer", address);
        createAndPrintOrder(customer);
        createAndPrintOrder(empty);
    }

	private static void createAndPrintOrder(Customer customer) {
		Order order = new Order(customer);

        // Wrap the root object in an Optional
        Optional<Order> orderOpt = Optional.ofNullable(order);

        // 1. Using map()
        // Order::getCustomer returns a plain 'Customer'. 
        // map() automatically wraps it into Optional<Customer>.
        Optional<Customer> customerOpt = orderOpt.map(Order::getCustomer);

        // 2. Using flatMap()
        // Customer::getShippingAddress ALREADY returns an Optional<ShippingAddress>.
        // If we used map(), we would get Optional<Optional<ShippingAddress>>.
        // flatMap() flattens it down into a single Optional<ShippingAddress>.
        Optional<ShippingAddress> addressOpt = customerOpt.flatMap(Customer::getShippingAddress);

        // 3. Chaining them together seamlessly
        String city = orderOpt
            .map(Order::getCustomer)                 // Returns Customer -> map wraps to Optional<Customer>
            .flatMap(Customer::getShippingAddress)   // Returns Optional<ShippingAddress> -> flatMap prevents nesting
            .map(ShippingAddress::getCity)           // Returns String -> map wraps to Optional<String>
            .orElseGet(() -> "Unknown City");                 // Fallback if anything along the chain was null/empty

        String output = String.format("Customer %s Shipping City: %s", 
        		customerOpt.orElseGet(() -> empty), city);//"Shipping City: " + city;
        //String output = "Shipping City: %s".formatted(city);
		System.out.println(output); // Prints: Shipping City: Los Angeles
	}
}
