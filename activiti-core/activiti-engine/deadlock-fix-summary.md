# Fix: PostgreSQL Deadlock During Concurrent Entity Flush Operations

## Problem

When parallel multi-instance subprocess instances complete concurrently, each completion runs in its own database transaction. During the flush phase, `DbSqlSession` processes entities in non-deterministic order (HashMap iteration for updates, LinkedHashMap insertion order for inserts/deletes). Two concurrent transactions operating on overlapping sets of rows can acquire locks in opposite orders, causing a circular wait that PostgreSQL detects as a deadlock.

Three observed deadlock variants:

1. **Deadlock on `act_ru_variable` during UPDATE** — two transactions update the same parent's counter variables (`nrOfCompletedInstances`, `nrOfActiveInstances`) in different order
2. **Deadlock on `act_ru_execution` during UPDATE** — cross-table: one transaction flushes execution updates before variable updates, the other flushes in the opposite order
3. **Deadlock on `act_ru_execution` during DELETE** — two transactions delete different execution rows within the same table in different order

## Root Cause

- `flushUpdates()` built its `updatedObjects` list from `entityCache.getAllCachedEntities()` which returns `Map<Class<?>, Map<String, CachedEntity>>` — both HashMap levels have undefined iteration order
- `flushInsertEntities()` and `flushDeleteEntities()` iterated entities within each class in LinkedHashMap insertion order, which can vary across concurrent transactions depending on the order entities were loaded into the cache

## Files Modified

### `EntityDependencyOrder.java`

Added `UPDATE_ORDER` field, initialized from `INSERT_ORDER` in the static block. This provides a canonical class-level ordering for updates, matching the existing pattern used by `INSERT_ORDER` and `DELETE_ORDER`.

```java
public static List<Class<? extends Entity>> UPDATE_ORDER = new ArrayList<>();

static {
    // ... existing DELETE_ORDER and INSERT_ORDER initialization ...
    UPDATE_ORDER = new ArrayList<>(INSERT_ORDER);
}
```

### `DbSqlSession.java`

Three methods modified:

#### `flushUpdates()` — sort `updatedObjects` before executing SQL

Sorts by: (1) `UPDATE_ORDER` class position, (2) class name, (3) entity ID. This ensures all concurrent transactions acquire row locks in the same global order, preventing both cross-table and within-table deadlocks.

```java
updatedObjects.sort(
    Comparator.comparingInt((Entity e) -> {
        int idx = EntityDependencyOrder.UPDATE_ORDER.indexOf(e.getClass());
        return idx >= 0 ? idx : Integer.MAX_VALUE;
    })
    .thenComparing(e -> e.getClass().getName())
    .thenComparing(Entity::getId)
);
```

#### `flushInsertEntities()` — sort entities by ID within each class

Sorts entities by ID before executing INSERT statements, ensuring deterministic lock acquisition order within the same table. `ExecutionEntity` is excluded because it has self-referential FK constraints (`PROC_INST_ID_` references `ID_`) that require parent-before-child insertion order.

```java
Collection<Entity> orderedInserts = entitiesToInsert;
if (!ExecutionEntity.class.isAssignableFrom(entityClass) && entitiesToInsert.size() > 1) {
    List<Entity> sortedInserts = new ArrayList<>(entitiesToInsert);
    sortedInserts.sort(Comparator.comparing(Entity::getId));
    orderedInserts = sortedInserts;
}
```

#### `flushDeleteEntities()` — sort entities by ID within each class

Same pattern as inserts. Sorts entities by ID before executing DELETE statements. `ExecutionEntity` is excluded for the same FK constraint reason (child must be deleted before parent).

```java
Collection<Entity> orderedDeletes = entitiesToDelete;
if (!ExecutionEntity.class.isAssignableFrom(entityClass) && entitiesToDelete.size() > 1) {
    List<Entity> sortedDeletes = new ArrayList<>(entitiesToDelete);
    sortedDeletes.sort(Comparator.comparing(Entity::getId));
    orderedDeletes = sortedDeletes;
}
```

## Why ExecutionEntity Is Excluded from Insert/Delete Sorting

`ExecutionEntity` has a self-referential foreign key: `ACT_FK_EXE_PROCINST` where `PROC_INST_ID_` references `ACT_RU_EXECUTION(ID_)`. Sorting by ID (string comparison) can place a child execution before its parent — e.g., `"10" < "4"` in string order — causing FK constraint violations. The existing cross-class ordering via `INSERT_ORDER`/`DELETE_ORDER` already handles the inter-table dependency; the within-class parent-child ordering for executions relies on insertion order which naturally respects the hierarchy.

## Risk Assessment

Low. The previous iteration order was explicitly non-deterministic. No code depends on any particular order of updates, inserts, or deletes within the same class. The optimistic lock check (`REV_ = #{revision}`) is order-independent. The entity lists are typically small (5-20 entities), so sorting overhead is negligible compared to SQL execution time.
