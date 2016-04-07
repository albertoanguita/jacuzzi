package jacz.util.io.serialization.localstorage;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

/**
 * The table for user-made items handled by the local storage
 */
@Table("items")
public class Item extends Model {
}
