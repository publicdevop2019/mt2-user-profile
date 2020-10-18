package com.hw.aggregate.address;

import com.hw.aggregate.address.command.UserCreateBizAddressCommand;
import com.hw.aggregate.address.command.UserUpdateBizAddressCommand;
import com.hw.shared.ServiceUtility;
import com.hw.shared.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.hw.shared.AppConstant.*;

@Slf4j
@RestController
@RequestMapping(produces = "application/json", path = "addresses")
public class BizAddressController {

    @Autowired
    private AdminBizAddressApplicationService adminBizAddressApplicationService;
    @Autowired
    private UserBizAddressApplicationService userBizAddressApplicationService;

    @GetMapping("admin")
    public ResponseEntity<?> readForAdminByQuery(
            @RequestParam(value = HTTP_PARAM_QUERY, required = false) String queryParam,
            @RequestParam(value = HTTP_PARAM_PAGE, required = false) String pageParam,
            @RequestParam(value = HTTP_PARAM_SKIP_COUNT, required = false) String skipCount
    ) {
        return ResponseEntity.ok(adminBizAddressApplicationService.readByQuery(queryParam, pageParam, skipCount));
    }

    @GetMapping("admin/{id}")
    public ResponseEntity<?> readForAdminById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminBizAddressApplicationService.readById(id));
    }

    @GetMapping("user")
    public ResponseEntity<?> readForUserByQuery(@RequestHeader("authorization") String authorization
    ) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok(userBizAddressApplicationService.readByQuery(null, null, null));
    }

    @GetMapping("user/{id}")
    public ResponseEntity<?> readForUserById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok(userBizAddressApplicationService.readById(id));
    }

    @PostMapping("user")
    public ResponseEntity<?> createForUser(@RequestHeader("authorization") String authorization, @RequestBody UserCreateBizAddressCommand command, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        return ResponseEntity.ok().header("Location", userBizAddressApplicationService.create(command, changeId).getId().toString()).build();
    }


    @PutMapping("user/{id}")
    public ResponseEntity<?> replaceForUserById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestBody UserUpdateBizAddressCommand command, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        userBizAddressApplicationService.replaceById(id, command, changeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("user/{id}")
    public ResponseEntity<?> deleteForAdminById(@RequestHeader("authorization") String authorization, @PathVariable(name = "id") Long id, @RequestHeader(HTTP_HEADER_CHANGE_ID) String changeId) {
        UserThreadLocal.unset();
        UserThreadLocal.set(ServiceUtility.getUserId(authorization));
        userBizAddressApplicationService.deleteById(id, changeId);
        return ResponseEntity.ok().build();
    }

}
