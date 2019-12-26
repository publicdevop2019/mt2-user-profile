package com.hw.clazz;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class ProductOption {
    public String title;
    public List<OptionItem> options;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductOption that = (ProductOption) o;
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
