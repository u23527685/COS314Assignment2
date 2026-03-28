public class Item {
    public float wheight;
    public float value;

    /**checks if two items are the same**/
    public boolean equals(Item other){
        return(wheight==other.wheight) && (value==other.value);
    }

    /**checks if an item has more wheight than the current item**/
    public boolean validate(Item other){
        return(wheight>=other.wheight);
    }

    /** constructor for item **/
    public Item(float wheight, float value){
        this.wheight=wheight;
        this.value=value;
    }

    public void print(){
        System.out.println("Wheight: "+wheight+" , Value: "+value);
    }
}
