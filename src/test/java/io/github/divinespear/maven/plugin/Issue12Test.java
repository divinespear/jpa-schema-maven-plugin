package io.github.divinespear.maven.plugin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class Issue12Test
        extends AbstractSchemaGeneratorMojoTest {

    @Test
    public void testShouldFormatCreateTable() {
        String from = "CREATE TABLE SYSTEM_CURRENCY_RATE_HISTORY (CREATED_DATE DATETIME NULL,LAST_MODIFIED_DATE DATETIME NULL,RATE NUMERIC(28) NULL,VERSION NUMERIC(19) NOT NULL,REFERENCE_ID VARCHAR(255) NOT NULL,CREATED_BY VARCHAR(36) NULL,LAST_MODIFIED_BY VARCHAR(36) NULL,PRIMARY KEY (VERSION,REFERENCE_ID));";
        String expected = "CREATE TABLE SYSTEM_CURRENCY_RATE_HISTORY (" + LINE_SEPARATOR
                          + "\tCREATED_DATE DATETIME NULL," + LINE_SEPARATOR
                          + "\tLAST_MODIFIED_DATE DATETIME NULL," + LINE_SEPARATOR
                          + "\tRATE NUMERIC(28) NULL," + LINE_SEPARATOR
                          + "\tVERSION NUMERIC(19) NOT NULL," + LINE_SEPARATOR
                          + "\tREFERENCE_ID VARCHAR(255) NOT NULL," + LINE_SEPARATOR
                          + "\tCREATED_BY VARCHAR(36) NULL," + LINE_SEPARATOR
                          + "\tLAST_MODIFIED_BY VARCHAR(36) NULL," + LINE_SEPARATOR
                          + "\tPRIMARY KEY (VERSION,REFERENCE_ID)" + LINE_SEPARATOR
                          + ");";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void testShouldFormatCreateTableEx() {
        String from = "CREATE TEMPORARY TABLE SYSTEM_CURRENCY_RATE_HISTORY (CREATED_DATE DATETIME NULL,LAST_MODIFIED_DATE DATETIME NULL,RATE NUMERIC(28) NULL,VERSION NUMERIC(19) NOT NULL,REFERENCE_ID VARCHAR(255) NOT NULL,CREATED_BY VARCHAR(36) NULL,LAST_MODIFIED_BY VARCHAR(36) NULL,PRIMARY KEY (VERSION,REFERENCE_ID));";
        String expected = "CREATE TEMPORARY TABLE SYSTEM_CURRENCY_RATE_HISTORY (" + LINE_SEPARATOR
                          + "\tCREATED_DATE DATETIME NULL," + LINE_SEPARATOR
                          + "\tLAST_MODIFIED_DATE DATETIME NULL," + LINE_SEPARATOR
                          + "\tRATE NUMERIC(28) NULL," + LINE_SEPARATOR
                          + "\tVERSION NUMERIC(19) NOT NULL," + LINE_SEPARATOR
                          + "\tREFERENCE_ID VARCHAR(255) NOT NULL," + LINE_SEPARATOR
                          + "\tCREATED_BY VARCHAR(36) NULL," + LINE_SEPARATOR
                          + "\tLAST_MODIFIED_BY VARCHAR(36) NULL," + LINE_SEPARATOR
                          + "\tPRIMARY KEY (VERSION,REFERENCE_ID)" + LINE_SEPARATOR
                          + ");";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void testShouldFormatCreateIndex() {
        String from = "CREATE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED ON USER_ACCOUNT (ENABLED,DELETED);";
        String expected = "CREATE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED" + LINE_SEPARATOR
                          + "\tON USER_ACCOUNT (ENABLED,DELETED);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void testShouldFormatCreateIndexEx() {
        String from = "CREATE UNIQUE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED ON USER_ACCOUNT (ENABLED,DELETED);";
        String expected = "CREATE UNIQUE INDEX INDEX_USER_ACCOUNT_ENABLED_DELETED" + LINE_SEPARATOR
                          + "\tON USER_ACCOUNT (ENABLED,DELETED);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void testShouldFormatAlterTable() {
        String from = "ALTER TABLE PRODUCT_CATEGORY ADD CONSTRAINT PRODUCTCATEGORYPRENTID FOREIGN KEY (PARENT_ID) REFERENCES PRODUCT_CATEGORY (ID);";
        String expected = "ALTER TABLE PRODUCT_CATEGORY" + LINE_SEPARATOR
                          + "\tADD CONSTRAINT PRODUCTCATEGORYPRENTID FOREIGN KEY (PARENT_ID)" + LINE_SEPARATOR
                          + "\tREFERENCES PRODUCT_CATEGORY (ID);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }

    @Test
    public void testShouldOverrideCreateIndex() {
        String from = "CREATE INDEX INDEX_SYSTEM_CURRENCY_RATE_VERSION DESC ON SYSTEM_CURRENCY_RATE (VERSION DESC);";
        String expected = "CREATE INDEX INDEX_SYSTEM_CURRENCY_RATE_VERSION" + LINE_SEPARATOR
                          + "\tON SYSTEM_CURRENCY_RATE (VERSION DESC);";
        JpaSchemaGeneratorMojo mojo = new JpaSchemaGeneratorMojo();
        assertThat(mojo.format(from), is(expected));
    }
}
