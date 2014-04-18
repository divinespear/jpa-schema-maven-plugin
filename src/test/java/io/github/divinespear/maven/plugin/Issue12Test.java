package io.github.divinespear.maven.plugin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class Issue12Test {

    @Test
    public void shouldFormatCreateTable() {
        String from = "CREATE TABLE SYSTEM_CURRENCY_RATE_HISTORY (CREATED_DATE DATETIME NULL,LAST_MODIFIED_DATE DATETIME NULL,RATE NUMERIC(28) NULL,VERSION NUMERIC(19) NOT NULL,REFERENCE_ID VARCHAR(255) NOT NULL,CREATED_BY VARCHAR(36) NULL,LAST_MODIFIED_BY VARCHAR(36) NULL,PRIMARY KEY (VERSION,REFERENCE_ID));";
        String expected = "CREATE TABLE SYSTEM_CURRENCY_RATE_HISTORY (\r\n"
                          + "\tCREATED_DATE DATETIME NULL,\r\n"
                          + "\tLAST_MODIFIED_DATE DATETIME NULL,\r\n"
                          + "\tRATE NUMERIC(28) NULL,\r\n"
                          + "\tVERSION NUMERIC(19) NOT NULL,\r\n"
                          + "\tREFERENCE_ID VARCHAR(255) NOT NULL,\r\n"
                          + "\tCREATED_BY VARCHAR(36) NULL,\r\n"
                          + "\tLAST_MODIFIED_BY VARCHAR(36) NULL,\r\n"
                          + "\tPRIMARY KEY (VERSION,REFERENCE_ID)\r\n"
                          + ");";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void shouldFormatCreateIndex() {
        String from = "CREATE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED ON USER_ACCOUNT (ENABLED,DELETED);";
        String expected = "CREATE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED\r\n"
                          + "\tON USER_ACCOUNT (ENABLED,DELETED);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void shouldFormatAlterTable() {
        String from = "ALTER TABLE PRODUCT_CATEGORY ADD CONSTRAINT PRODUCTCATEGORYPRENTID FOREIGN KEY (PARENT_ID) REFERENCES PRODUCT_CATEGORY (ID);";
        String expected = "ALTER TABLE PRODUCT_CATEGORY\r\n"
                          + "\tADD CONSTRAINT PRODUCTCATEGORYPRENTID FOREIGN KEY (PARENT_ID)\r\n"
                          + "\tREFERENCES PRODUCT_CATEGORY (ID);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }
}
