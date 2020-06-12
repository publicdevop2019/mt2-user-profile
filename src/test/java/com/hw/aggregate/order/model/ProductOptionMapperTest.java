package com.hw.aggregate.order.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.hw.aggregate.Helper.rStr;

public class ProductOptionMapperTest {

    @Test
    public void convertToDatabaseColumn() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        String s = productOptionMapper.convertToDatabaseColumn(new ArrayList<>());
        Assert.assertEquals("", s);
    }

    @Test
    public void convertToDatabaseColumn_1() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        String s = productOptionMapper.convertToDatabaseColumn(null);
        Assert.assertEquals("", s);
    }

    @Test
    public void convertToDatabaseColumn_2() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        String s = productOptionMapper.convertToDatabaseColumn(getAddOn());
        Assert.assertNotNull(s);
    }

    @Test
    public void convertToEntityAttribute() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        List<BizOrderItemAddOn> customerOrderItemAddOns = productOptionMapper.convertToEntityAttribute("");
        Assert.assertNotNull(customerOrderItemAddOns);
        Assert.assertEquals(0, customerOrderItemAddOns.size());
    }

    @Test
    public void convertToEntityAttribute_2() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        List<BizOrderItemAddOn> customerOrderItemAddOns = productOptionMapper.convertToEntityAttribute("qty:1&1=2&2=3&3,color:white&0.35=black&0.37");
        Assert.assertNotNull(customerOrderItemAddOns);
        Assert.assertNotEquals(0, customerOrderItemAddOns.size());
    }

    @Test
    public void convertToEntityAttribute_3() {
        ProductOptionMapper productOptionMapper = new ProductOptionMapper();
        List<BizOrderItemAddOn> customerOrderItemAddOns = productOptionMapper.convertToEntityAttribute("qty:1&=2&2=3&3,color:white&0.35=black&0.37");
        Assert.assertNotNull(customerOrderItemAddOns);
        Assert.assertNotEquals(0, customerOrderItemAddOns.size());
    }

    private List<BizOrderItemAddOn> getAddOn() {
        List<BizOrderItemAddOn> customerOrderItemAddOns = new ArrayList<>();
        BizOrderItemAddOn customerOrderItemAddOn0 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn1 = new BizOrderItemAddOn();
        BizOrderItemAddOn customerOrderItemAddOn2 = new BizOrderItemAddOn();
        customerOrderItemAddOn0.setTitle(rStr());
        customerOrderItemAddOn1.setTitle(rStr());
        customerOrderItemAddOn2.setTitle(rStr());
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections0 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections1 = new ArrayList<>();
        List<BizOrderItemAddOnSelection> customerOrderItemAddOnSelections2 = new ArrayList<>();
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection0 = new BizOrderItemAddOnSelection(rStr(), null);
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection1 = new BizOrderItemAddOnSelection(rStr(), "+0");
        BizOrderItemAddOnSelection customerOrderItemAddOnSelection2 = new BizOrderItemAddOnSelection(rStr(), "+0");
        customerOrderItemAddOnSelections0.add(customerOrderItemAddOnSelection0);
        customerOrderItemAddOnSelections1.add(customerOrderItemAddOnSelection1);
        customerOrderItemAddOnSelections2.add(customerOrderItemAddOnSelection2);
        customerOrderItemAddOn0.setOptions(customerOrderItemAddOnSelections0);
        customerOrderItemAddOn1.setOptions(customerOrderItemAddOnSelections1);
        customerOrderItemAddOn2.setOptions(customerOrderItemAddOnSelections2);
        customerOrderItemAddOns.add(customerOrderItemAddOn0);
        customerOrderItemAddOns.add(customerOrderItemAddOn1);
        customerOrderItemAddOns.add(customerOrderItemAddOn2);
        return customerOrderItemAddOns;
    }
}