import java.io.*; import java.util.*; import java.util.stream.*;

class Item { int id; String name; String category; double price;

Item(int id, String name, String category, double price) {
    this.id = id;
    this.name = name;
    this.category = category;
    this.price = price;
}

}

class CartItem { Item item; int quantity;

CartItem(Item item, int quantity) {
    this.item = item;
    this.quantity = quantity;
}

double getTotal() {
    return item.price * quantity;
}

}

public class EcommerceCLI { static Map<Integer, Item> products = new HashMap<>(); static Map<String, Double> coupons = new HashMap<>(); static List<CartItem> cart = new ArrayList<>(); static Scanner sc = new Scanner(System.in);

public static void main(String[] args) throws Exception {
    loadProducts();
    loadCoupons();

    while (true) {
        System.out.println("\n1. View Products\n2. Add to Cart\n3. View Cart\n4. Checkout\n5. Exit\n6. Sort Products");
        int choice = sc.nextInt();

        switch (choice) {
            case 1 -> viewProducts();
            case 2 -> addToCart();
            case 3 -> viewCart();
            case 4 -> checkout();
            case 5 -> exitProgram();
            case 6 -> sortProductsMenu();
            default -> System.out.println("Invalid choice");
        }
    }
}

static void loadProducts() throws Exception {
    BufferedReader br = new BufferedReader(new FileReader("products.csv"));
    String line;
    while ((line = br.readLine()) != null) {
        String[] p = line.split(",");
        products.put(Integer.parseInt(p[0]), new Item(
                Integer.parseInt(p[0]), p[1], p[2], Double.parseDouble(p[3])
        ));
    }
    br.close();
}

static void loadCoupons() throws Exception {
    BufferedReader br = new BufferedReader(new FileReader("coupons.csv"));
    String line;
    while ((line = br.readLine()) != null) {
        String[] c = line.split(",");
        coupons.put(c[0], Double.parseDouble(c[1]));
    }
    br.close();
}

static void viewProducts() {
    products.values().forEach(p ->
            System.out.println(p.id + " | " + p.name + " | " + p.category + " | ₹" + p.price)
    );
}

static void addToCart() {
    System.out.print("Enter Product ID: ");
    int id = sc.nextInt();
    System.out.print("Enter Quantity: ");
    int qty = sc.nextInt();

    Item item = products.get(id);
    if (item != null && qty > 0) {
        cart.add(new CartItem(item, qty));
        System.out.println("Added to cart");
    } else {
        System.out.println("Invalid product or quantity");
    }
}

static void viewCart() {
    if (cart.isEmpty()) {
        System.out.println("Cart is empty");
        return;
    }

    cart.forEach(c -> System.out.println(c.item.name + " x" + c.quantity + " = ₹" + c.getTotal()));
    double subtotal = cart.stream().mapToDouble(CartItem::getTotal).sum();
    System.out.println("Subtotal: ₹" + subtotal);

    // Show category-wise totals
    Map<String, Double> categoryTotals = new HashMap<>();
    for (CartItem c : cart) {
        categoryTotals.put(c.item.category, categoryTotals.getOrDefault(c.item.category, 0.0) + c.getTotal());
    }
    System.out.println("Category-wise Totals:");
    categoryTotals.forEach((cat, total) -> System.out.println(cat + ": ₹" + total));

    // Show most expensive item
    CartItem maxItem = Collections.max(cart, Comparator.comparingDouble(CartItem::getTotal));
    System.out.println("Most expensive item: " + maxItem.item.name + " x" + maxItem.quantity + " = ₹" + maxItem.getTotal());
}

static void checkout() throws Exception {
    if (cart.isEmpty()) {
        System.out.println("Cart is empty. Add products first.");
        return;
    }

    double subtotal = cart.stream().mapToDouble(CartItem::getTotal).sum();

    System.out.print("Enter coupon code (or NONE): ");
    String code = sc.next();
    double discountValue = coupons.getOrDefault(code, 0.0);
    double discountAmount = (discountValue <= 1) ? discountValue : subtotal * discountValue / 100;

    double afterDiscount = subtotal - discountAmount;
    double gst = afterDiscount * 0.18;
    double total = afterDiscount + gst;

    generateInvoice(subtotal, discountAmount, gst, total);
    cart.clear();
}

static void generateInvoice(double subtotal, double discount, double gst, double total) throws Exception {
    String fileName = "invoice_" + System.currentTimeMillis() + ".txt";
    PrintWriter pw = new PrintWriter(fileName);

    pw.println("------ INVOICE ------");
    cart.forEach(c -> pw.println(c.item.name + " x" + c.quantity + " = ₹" + c.getTotal()));
    pw.println("Subtotal: ₹" + subtotal);
    pw.println("Discount: ₹" + discount);
    pw.println("GST (18%): ₹" + gst);
    pw.println("Total: ₹" + total);
    pw.println("---------------------");

    pw.close();
    System.out.println("Invoice generated: " + fileName);
}

static void exitProgram() {
    System.out.print("Are you sure you want to exit? (Y/N): ");
    String ans = sc.next();
    if (ans.equalsIgnoreCase("Y")) {
        System.out.println("Exiting...");
        System.exit(0);
    }
}

static void sortProductsMenu() {
    System.out.println("Sort Products by:\n1. Price Low to High\n2. Price High to Low\n3. Category");
    int choice = sc.nextInt();
    List<Item> list = new ArrayList<>(products.values());

    switch (choice) {
        case 1 -> list.sort(Comparator.comparingDouble(i -> i.price));
        case 2 -> list.sort((i1, i2) -> Double.compare(i2.price, i1.price));
        case 3 -> list.sort(Comparator.comparing(i -> i.category));
        default -> System.out.println("Invalid choice");
    }

    list.forEach(p -> System.out.println(p.id + " | " + p.name + " | " + p.category + " | ₹" + p.price));
}

}