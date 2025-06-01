package Algo.sort;
class MainTestSelect {
    public static void main( String[ ] args ) {
        int[ ] input = { 546, 743, 5, 394,  4, 11, 42 };
        System.out.println(Select.randomizedSelect(input, 0));
        System.out.println(Select.randomizedSelect(input, 1));
        System.out.println(Select.randomizedSelect(input, 2));
        input = new int[]{ 546, 743, 5, 394,  4, 11, 42 };
        System.out.println(Select.randomizedSelect(input, 0, 0, 3));
        System.out.println(Select.randomizedSelect(input, 1, 0, 3));
        System.out.println(Select.randomizedSelect(input, 2, 0, 3));
    }
}

