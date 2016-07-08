package org.aanguita.jacuzzi.io.serialization.localstorage;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.DbName;
import org.javalite.activejdbc.annotations.Table;

/**
 * Metadata table
 */
@DbName(LocalStorage.DATABASE)
@Table(LocalStorage.METADATA_TABLE)
public class Metadata extends Model {
}
