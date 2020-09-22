package com.hw.aggregate.cart;

import com.hw.aggregate.address.model.UserThreadLocal;
import com.hw.aggregate.cart.command.UserCreateBizCartItemCommand;
import com.hw.shared.ServiceUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hw.shared.AppConstant.HTTP_HEADER_CHANGE_ID;

@RestController
@RequestMapping(produces = "application/json", path = "cart")
public class BizCartController {

    @Autowired
    private UserBizCartApplicationService userBizCartApplicationService;

    @GetMapping("user")
    public ResponseEntity<?> readForUserByQuery(@RequestHeader("authorization") String authorization) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok(userBizCartApplicationService.readByQuery(null, null, null));
    }

    @PostMapping("user")
    public ResponseEntity<Void> createForUser(@RequestHeader("authorization") String authorization, @RequestBody UserCreateBizCartItemCommand command, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok().header("Location", userBizCartApplicationService.create(command, changeId).getId().toString()).build();
    }

    @DeleteMapping("user/{id}")
    public ResponseEntity<Void> deleteForUserById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        userBizCartApplicationService.deleteById(id, changeId);
        return ResponseEntity.ok().build();
    }
}
