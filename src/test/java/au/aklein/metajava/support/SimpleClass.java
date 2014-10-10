package au.aklein.metajava.support;

/**
 * Test class to verify that classes are being read correctly
 */
public class SimpleClass implements Runnable {

    private int x;
    private boolean var;

    public SimpleClass(int x) {
        this.x = x;
    }


    public void testMethod() {
        x++;
    }

    public static boolean staticTestMethod() {
        return false;
    }

    @Override
    public void run() {
        //Do things
    }

    public class SimpleInnerClass {
        private short innerField;
    }



}
