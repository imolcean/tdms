SELECT r.table_name, r.row_count, r.[object_id]
FROM sys.tables t
INNER JOIN (
    SELECT OBJECT_NAME(s.[object_id]) table_name, SUM(s.row_count) row_count, s.[object_id]
    FROM sys.dm_db_partition_stats s
    WHERE s.index_id in (0,1)
    GROUP BY s.[object_id]
) r on t.[object_id] = r.[object_id]
WHERE r.row_count > 0
ORDER BY r.table_name;
