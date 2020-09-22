package com.hw.aggregate.task;

import com.hw.aggregate.task.representation.AdminBizTaskCardRep;
import com.hw.shared.sql.SumPagedRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.hw.shared.AppConstant.*;

@RestController
@RequestMapping(produces = "application/json", path = "tasks")
public class BizTaskController {
    @Autowired
    AdminBizTaskApplicationService adminBizTaskApplicationService;


    @GetMapping("admin")
    public ResponseEntity<SumPagedRep<AdminBizTaskCardRep>> readForAdminByQuery(@RequestParam(value = HTTP_PARAM_QUERY, required = false) String queryParam,
                                                                                @RequestParam(value = HTTP_PARAM_PAGE, required = false) String pageParam,
                                                                                @RequestParam(value = HTTP_PARAM_SKIP_COUNT, required = false) String config) {
        return ResponseEntity.ok(adminBizTaskApplicationService.readByQuery(queryParam, pageParam, config));
    }

}
