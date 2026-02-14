package sqlbuilder.dialects;

import sqlbuilder.exceptions.ValueCannotBeEmptyException;

import java.util.Set;

public interface SqlDialect {
    default public String quote(String identifier) {
        return "\"" + identifier + "\"";
    }
    default public String formatTableIdentifier(String tableIdentifier, Set<String> tableContext) {
        return tableIdentifier;
    }

    /**
     * Applies paging with an offset to the SQL statement using the dialect specific syntax
     *
     * @param limit The limit of how many entries the SQL statement will return
     * @param offset The offset at which the limit counting starts
     * @return the paging statement
     */
    public String applyPaging(int limit, int offset);

    public class OracleDialect implements SqlDialect {

        @Override
        public String applyPaging(int limit, int offset) {
            return " LIMIT " + limit + " OFFSET " + offset;
        }
    }

    public class DB2Dialect implements SqlDialect {
        @Override
        public String applyPaging(int limit, int offset) {
            //TODO implement paging syntax
            return "";
        }

        @Override
        public String formatTableIdentifier(String tableIdentifier, Set<String> tableContext) {
            if(tableIdentifier == null || tableIdentifier.isBlank()) {
                throw new ValueCannotBeEmptyException("tableIdentifier");
            }

            for(String tableName : tableContext) {
                String prefix = tableName + ".";

                if(tableIdentifier.startsWith(prefix)) {
                    return tableIdentifier.substring(prefix.length());
                }
            }

            return tableIdentifier;
        }
    }

    public class MsSQLDialect implements SqlDialect {
        @Override
        public String applyPaging(int limit, int offset) {
            //TODO implement paging syntax
            return "";
        }
    }

    public class PostgresDialect implements SqlDialect {
        @Override
        public String applyPaging(int limit, int offset) {
            return " LIMIT " + limit + " OFFSET " + offset;
        }
    }

    public class H2Dialect implements SqlDialect {
        @Override
        public String applyPaging(int limit, int offset) {
            return " LIMIT " + limit + " OFFSET " + offset;
        }
    }
}