public class Electronics extends Item {
    private int warrantyMonths;

    public Electronics(String id, String name, double startingPrice, int warrantyMonths) {
        super(id, name, startingPrice);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("[Electronics] " + name + " | Start: " + startingPrice + " | Warranty: " + warrantyMonths + " months");
    }
}