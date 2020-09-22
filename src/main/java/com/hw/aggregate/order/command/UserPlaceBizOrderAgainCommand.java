package com.hw.aggregate.order.command;

import com.hw.aggregate.order.model.BizOrderAddressCmdRep;
import lombok.Data;

@Data
public class UserPlaceBizOrderAgainCommand {
    private BizOrderAddressCmdRep address;
}
