package com.hw.aggregate.address.model;

import com.hw.aggregate.address.UserBizAddressApplicationService;
import com.hw.aggregate.address.command.UserCreateBizAddressCommand;
import com.hw.aggregate.address.command.UserUpdateBizAddressCommand;
import com.hw.aggregate.address.exception.DuplicateAddressException;
import com.hw.aggregate.address.exception.MaxAddressCountException;
import com.hw.aggregate.address.representation.UserBizAddressCardRep;
import com.hw.shared.Auditable;
import com.hw.shared.rest.IdBasedEntity;
import com.hw.shared.sql.SumPagedRep;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Entity
@Table(name = "biz_address")
@Data
@NoArgsConstructor
public class BizAddress extends Auditable implements IdBasedEntity {
    @Id
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank
    @Column(nullable = false)
    private String line1;

    private String line2;

    @NotBlank
    @Column(nullable = false)
    private String postalCode;

    @NotBlank
    @Column(nullable = false)
    private String phoneNumber;

    @NotBlank
    @Column(nullable = false)
    private String city;

    @NotBlank
    @Column(nullable = false)
    private String province;

    @NotBlank
    @Column(nullable = false)
    private String country;

    public static BizAddress create(Long id, UserCreateBizAddressCommand command, UserBizAddressApplicationService userBizAddressApplicationService) {
        SumPagedRep<UserBizAddressCardRep> userBizAddressCardRepSumPagedRep = userBizAddressApplicationService.readByQuery(null, null, null);
        if (userBizAddressCardRepSumPagedRep.getTotalItemCount() == 5L)
            throw new MaxAddressCountException();
        if (userBizAddressCardRepSumPagedRep.getData().stream().anyMatch(e -> isDuplicateOf(command, e))) {
            throw new DuplicateAddressException();
        }
        return new BizAddress(id, command);
    }

    public BizAddress replace(UserUpdateBizAddressCommand command) {
        BeanUtils.copyProperties(command, this);
        return this;
    }

    private BizAddress(Long id, UserCreateBizAddressCommand command) {
        this.id = id;
        this.fullName = command.getFullName();
        this.line1 = command.getLine1();
        this.line2 = command.getLine2();
        this.postalCode = command.getPostalCode();
        this.phoneNumber = command.getPhoneNumber();
        this.city = command.getCity();
        this.province = command.getProvince();
        this.country = command.getCountry();
    }

    private static boolean isDuplicateOf(UserCreateBizAddressCommand command, UserBizAddressCardRep rep) {
        return Objects.equals(rep.getFullName(), command.getFullName()) &&
                Objects.equals(rep.getLine1(), command.getLine1()) &&
                Objects.equals(rep.getLine2(), command.getLine2()) &&
                Objects.equals(rep.getPostalCode(), command.getPostalCode()) &&
                Objects.equals(rep.getPhoneNumber(), command.getPhoneNumber()) &&
                Objects.equals(rep.getCity(), command.getCity()) &&
                Objects.equals(rep.getProvince(), command.getProvince()) &&
                Objects.equals(rep.getCountry(), command.getCountry());
    }
}
