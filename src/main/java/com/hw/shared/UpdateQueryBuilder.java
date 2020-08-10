package com.hw.shared;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.hw.shared.AppConstant.PATCH_OP_TYPE_DIFF;
import static com.hw.shared.AppConstant.PATCH_OP_TYPE_SUM;

public abstract class UpdateQueryBuilder<T> {
    protected EntityManager em;

    /**
     * sample :[
     * {op:'add',path:'/0001/name',value:'foo'},
     * {op:'add',path:'/0002/name',value:'foo'}
     * {op:'add',path:'/0003/name',value:'foo'}
     * {op:'add',path:'/0004/name',value:'foo'}
     * ]
     * sample2 :[
     * {op:'sum',path:'/0001/storageOrder',value:'1'},
     * {op:'sum',path:'/0001/storageOrder',value:'1'},
     * {op:'sum',path:'/0002/storageOrder',value:'1'}
     * ]
     * sample3 :[
     * {op:'diff',path:'/0001/storageOrder',value:'1'},
     * {op:'diff',path:'/0001/storageOrder',value:'1'},
     * {op:'diff',path:'/0002/storageOrder',value:'1'}
     * ]
     */
    public Integer update(List<PatchCommand> commands, Class<T> clazz) {
        Map<PatchCommand, List<String>> jsonPatchCommandListHashMap = optimizePatchCommands(commands);
        Map<PatchCommand, CriteriaUpdate<T>> patchCommandCriteriaUpdateHashMap = new LinkedHashMap<>();
        jsonPatchCommandListHashMap.keySet().forEach(comm -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaUpdate<T> criteriaUpdate = cb.createCriteriaUpdate(clazz);
            Root<T> root = criteriaUpdate.from(clazz);
            Predicate or = getWhereClause(root, jsonPatchCommandListHashMap.get(comm), comm);
            if (or != null)
                criteriaUpdate.where(or);
            setUpdateValue(root, criteriaUpdate, comm);
            patchCommandCriteriaUpdateHashMap.put(comm, criteriaUpdate);
        });
        AtomicInteger count = new AtomicInteger(0);
        patchCommandCriteriaUpdateHashMap.forEach((key, value) -> {
            int i = em.createQuery(value).executeUpdate();
            if (key.getExpect() == null) {
                count.addAndGet(i);
            } else {
                if (key.getExpect().equals(i)) {
                    count.addAndGet(i);
                } else {
                    throw new PatchCommandExpectNotMatchException();
                }
            }
        });
        return count.get();
    }

    private Map<PatchCommand, List<String>> optimizePatchCommands(List<PatchCommand> commands) {
        Map<PatchCommand, Integer> jsonPatchCommandCount = new HashMap<>();
        commands.forEach(e -> {
            if (jsonPatchCommandCount.containsKey(e)) {
                jsonPatchCommandCount.put(e, jsonPatchCommandCount.get(e) + 1);
            } else {
                jsonPatchCommandCount.put(e, 1);
            }
        });
        jsonPatchCommandCount.keySet().forEach(e -> {
            if (e.getOp().equalsIgnoreCase(PATCH_OP_TYPE_SUM) || e.getOp().equalsIgnoreCase(PATCH_OP_TYPE_DIFF)) {
                if (e.getValue() instanceof Integer) {
                    e.setValue((Integer) e.getValue() * jsonPatchCommandCount.get(e));
                } else {
                    e.setValue(Integer.parseInt((String) e.getValue()) * jsonPatchCommandCount.get(e));
                }
            }
        });
        Set<PatchCommand> patchCommands = jsonPatchCommandCount.keySet();

        Map<PatchCommand, List<String>> jsonPatchCommandListHashMap = new LinkedHashMap<>();

        // sort key so deadlock will not happen
        patchCommands.stream().sorted(PatchCommand::compareTo).forEach(e -> {
            String s = parseId(e.getPath());
            e.setPath(removeId(e.getPath()));
            if (jsonPatchCommandListHashMap.containsKey(e)) {
                List<String> strings = jsonPatchCommandListHashMap.get(e);
                strings.add(s);
            } else {
                ArrayList<String> strings = new ArrayList<>();
                strings.add(s);
                jsonPatchCommandListHashMap.put(e, strings);
            }
        });
        return jsonPatchCommandListHashMap;
    }

    private boolean hasMoreThanOne(PatchCommand e) {
        return false;
    }

    private String removeId(String path) {
        String[] split = path.split("/");
        List<String> collect = Arrays.stream(split).collect(Collectors.toList());
        collect.remove(0);
        collect.remove(0);
        return "/" + String.join("/", collect);
    }

    private String parseId(String path) {
        String[] split = path.split("/");
        return split[1];
    }

    protected abstract void setUpdateValue(Root<T> root, CriteriaUpdate<T> criteriaUpdate, PatchCommand operationLike);

    protected abstract Predicate getWhereClause(Root<T> root, List<String> ids, PatchCommand command);

}
