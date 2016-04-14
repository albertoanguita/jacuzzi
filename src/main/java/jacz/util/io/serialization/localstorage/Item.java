package jacz.util.io.serialization.localstorage;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;

/**
 * The table for user-made items handled by the local storage
 */
@DbName(LocalStorage.DATABASE)
public class Item extends Model {
}
