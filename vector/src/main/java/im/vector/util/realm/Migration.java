package im.vector.util.realm;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {
    public static final int DB_VERSION = 1;

    @Override
    public void migrate(@NotNull final DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        Log.w("Migration", "Number: " + oldVersion);
        if (oldVersion == 0) {
            RealmObjectSchema pinSchema = schema.create("KedrPin");
            pinSchema.addField("pin", String.class, FieldAttribute.PRIMARY_KEY);

            RealmObjectSchema roomSchema = schema.create("KedrRoom");
            roomSchema.addField("roomId", String.class, FieldAttribute.PRIMARY_KEY);
            roomSchema.addRealmObjectField("roomPin", pinSchema);
            oldVersion++;
        }
    }
}