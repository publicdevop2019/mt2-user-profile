package com.hw.shared;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

public abstract class UpdateQueryBuilder<T> {
    protected EntityManager em;

    /**
     * sample :[
     * {op:'replace',path:'/0001/name',value:'foo'},
     * {op:'replace',path:'/0002/name',value:'foo'}
     * {op:'replace',path:'/0003/name',value:'foo'}
     * {op:'replace',path:'/0004/name',value:'foo'}
     * {op:'replace',path:'/0003/title',value:'bar'}
     * {op:'replace',path:'/0002/title',value:'xyz'}
     * {op:'replace',path:'/0002/address',value:'zoo'}
     * ]
     */
    public Integer update(List<PatchCommand> commands, Class<T> clazz) {
        // sort key so deadlock will not happen
        Collections.sort(commands);

        Map<PatchCommand, List<String>> jsonPatchCommandListHashMap = new LinkedHashMap<>();

        commands.forEach(e -> {
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
        List<CriteriaUpdate<T>> criteriaUpdates = jsonPatchCommandListHashMap.keySet().stream().map(e -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaUpdate<T> criteriaUpdate = cb.createCriteriaUpdate(clazz);
            Root<T> root = criteriaUpdate.from(clazz);
            Predicate or = getWhereClause(root, jsonPatchCommandListHashMap.get(e),e);
            if (or != null)
                criteriaUpdate.where(or);
            setUpdateValue(root, criteriaUpdate, e);
            return criteriaUpdate;
        }).collect(Collectors.toList());
        // how to validate number of rows updated ?
        return criteriaUpdates.stream().map(e -> em.createQuery(e).executeUpdate()).reduce(0, Integer::sum);
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

    protected abstract Predicate getWhereClause(Root<T> root, List<String> ids, @Nullable PatchCommand command);

}
