package exam.project;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

	// write your code here
        System.out.println("Enter number:");
        System.out.println("1: Create new product");
        System.out.println("2: Edit existing product");
        System.out.println("3: Delete product");
        System.out.println("4: Receive product(s)");
        System.out.println("5: Ship product(s)");

        String command = input.next();
        System.out.println("You selected: " + command);
    }
}