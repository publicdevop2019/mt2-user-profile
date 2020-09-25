package com.hw.aggregate.order;

import com.hw.aggregate.address.command.UserUpdateBizAddressCommand;
import com.hw.aggregate.order.command.AppCreateBizOrderCommand;
import com.hw.aggregate.order.command.AppValidateBizOrderCommand;
import com.hw.aggregate.order.command.UserCreateBizOrderCommand;
import com.hw.aggregate.order.representation.BizOrderConfirmStatusRepresentation;
import com.hw.shared.ServiceUtility;
import com.hw.shared.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hw.shared.AppConstant.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json", path = "orders")
public class BizOrderController {

    @Autowired
    private AdminBizOrderApplicationService adminBizOrderApplicationService;
    @Autowired
    private UserBizOrderApplicationService userBizOrderApplicationService;
    @Autowired
    private AppBizOrderApplicationService appBizOrderApplicationService;

    @GetMapping("admin")
    public ResponseEntity<?> readForAdminByQuery(
            @RequestParam(value = HTTP_PARAM_QUERY, required = false) String queryParam,
            @RequestParam(value = HTTP_PARAM_PAGE, required = false) String pageParam,
            @RequestParam(value = HTTP_PARAM_SKIP_COUNT, required = false) String skipCount
    ) {
        return ResponseEntity.ok(adminBizOrderApplicationService.readByQuery(queryParam, pageParam, skipCount));
    }

    @GetMapping("admin/{id}")
    public ResponseEntity<?> readForAdminById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminBizOrderApplicationService.readById(id));
    }

    @GetMapping("user")
    public ResponseEntity<?> readForUserByQuery(
            @RequestHeader("authorization") String authorization,
            @RequestParam(value = HTTP_PARAM_QUERY, required = false) String queryParam,
            @RequestParam(value = HTTP_PARAM_PAGE, required = false) String pageParam,
            @RequestParam(value = HTTP_PARAM_SKIP_COUNT, required = false) String skipCount
    ) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok(userBizOrderApplicationService.readByQuery(queryParam, pageParam, skipCount));
    }

    @GetMapping("user/{id}")
    public ResponseEntity<?> readForUserById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok(userBizOrderApplicationService.readById(id));
    }

    @PostMapping("user")
    public ResponseEntity<Void> createForUser(
            @RequestHeader("authorization") String authorization,
            @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId,
            @RequestBody UserCreateBizOrderCommand command) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok().header("Location", userBizOrderApplicationService.prepareOrder(command, changeId).getPaymentLink()).build();
    }

    @PostMapping("app")
    public ResponseEntity<Void> createForUser(
            @RequestHeader("authorization") String authorization,
            @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId,
            @RequestBody AppCreateBizOrderCommand command) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        appBizOrderApplicationService.create(command, changeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("user/{id}")
    public ResponseEntity<Void> confirmPaymentForUser(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId,
                                                      @RequestBody UserUpdateBizAddressCommand command) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        userBizOrderApplicationService.replaceById(id, command, changeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("user/{id}/confirm")
    public ResponseEntity<BizOrderConfirmStatusRepresentation> confirmPaymentForUser(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        return ResponseEntity.ok(userBizOrderApplicationService.confirmPayment(id, ServiceUtility.getUserId(authorization), changeId));
    }

    @PutMapping("user/{id}/reserve")
    public ResponseEntity<Void> reserveForUser(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok().header("Location", userBizOrderApplicationService.reserve(id, ServiceUtility.getUserId(authorization), changeId).getPaymentLink()).build();
    }

    @PostMapping("app/validate")
    public ResponseEntity<Void> validateForApp(@RequestBody AppValidateBizOrderCommand command) {
        appBizOrderApplicationService.validate(command);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("user/{id}")
    public ResponseEntity<Void> deleteForUserById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        userBizOrderApplicationService.deleteById(id, changeId);
        return ResponseEntity.ok().build();
    }
}
