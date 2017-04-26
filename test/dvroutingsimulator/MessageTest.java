/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dvroutingsimulator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author thanhvu
 */
public class MessageTest {
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of toString method, of class Message.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Message instance = new Message("128.119.40.128",1234,"128.119.44.0",4321,15,"hello");
        String expResult = "15 128.119.40.128 128.119.44.0 1234 4321 hello";
        String result = instance.toString();
        assertEquals(expResult, result);
        
        instance.addRouter("1.1.1.1", 1111);
        instance.addRouter("2.2.2.2", 2222);
        expResult = "15 128.119.40.128 128.119.44.0 1234 4321 hello 1.1.1.1-1111 2.2.2.2-2222";
        result = instance.toString();
        assertEquals(expResult, result);
    }
    
    public MessageTest() {
        System.out.println("parse-text constructor");
        Message instance = new Message("15 128.119.40.128 128.119.44.0 1234 4321 hello 1.1.1.1-1111 2.2.2.2-2222");
        String expResult = "15 128.119.40.128 128.119.44.0 1234 4321 hello 1.1.1.1-1111 2.2.2.2-2222";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
