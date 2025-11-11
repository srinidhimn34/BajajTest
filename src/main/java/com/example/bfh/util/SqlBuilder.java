package com.example.bfh.util;

public final class SqlBuilder {
    private SqlBuilder() {}

    public static String buildQuestion1Sql() {
        return """
            SELECT 
                P.AMOUNT AS SALARY,
                CONCAT(E.FIRST_NAME, ' ', E.LAST_NAME) AS NAME,
                TIMESTAMPDIFF(YEAR, E.DOB, CURDATE()) AS AGE,
                D.DEPARTMENT_NAME
            FROM PAYMENTS P
            JOIN EMPLOYEE E ON P.EMP_ID = E.EMP_ID
            JOIN DEPARTMENT D ON E.DEPARTMENT = D.DEPARTMENT_ID
            WHERE DAY(P.PAYMENT_TIME) != 1
            ORDER BY P.AMOUNT DESC
            LIMIT 1;
        """;
    }

    public static String buildQuestion2Sql() {
        return """
            SELECT 
                E1.EMP_ID,
                E1.FIRST_NAME,
                E1.LAST_NAME,
                D.DEPARTMENT_NAME,
                COUNT(E2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
            FROM EMPLOYEE E1
            JOIN DEPARTMENT D ON E1.DEPARTMENT = D.DEPARTMENT_ID
            LEFT JOIN EMPLOYEE E2 
                ON E1.DEPARTMENT = E2.DEPARTMENT
               AND E2.DOB > E1.DOB
            GROUP BY 
                E1.EMP_ID, E1.FIRST_NAME, E1.LAST_NAME, D.DEPARTMENT_NAME
            ORDER BY E1.EMP_ID DESC;
        """;
    }
}
