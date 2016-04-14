package jacz.util.io.serialization.localstorage;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * Metadata table
 */
@DbName(LocalStorage.DATABASE)
@Table("metadata")
public class Metadata extends Model {
}
