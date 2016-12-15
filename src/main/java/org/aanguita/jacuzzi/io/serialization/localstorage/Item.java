package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * The table for user-made items handled by the local storage
 */
@DbName(DBLocalStorage.DATABASE)
@Table(DBLocalStorage.ITEMS_TABLE)
public class Item extends Model {
}
