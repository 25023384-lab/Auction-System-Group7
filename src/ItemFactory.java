package src;
public class ItemFactory {
    public static Item createItem(String type, String id, String name, double price, int extraAttr) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(id, name, price, extraAttr);
            case "art":
                return new Art(id, name, price, String.valueOf(extraAttr));
            default:
                throw new IllegalArgumentException("Unknown item type");
        }
    }
}