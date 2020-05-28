package com.hw.aggregate.order.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Data
public class CustomerOrderItemAddOn implements Serializable {

    private static final long serialVersionUID = 1;

    private String title;

    private List<CustomerOrderItemAddOnSelection> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerOrderItemAddOn that = (CustomerOrderItemAddOn) o;
        return Objects.equals(title, that.title) &&
                /**
                 * use deepEquals for JPA persistentBag workaround, otherwise equals will return incorrect result
                 */
                Objects.deepEquals(options.toArray(), that.options.toArray());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, options);
    }
}
