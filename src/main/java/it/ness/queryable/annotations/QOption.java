package it.ness.queryable.annotations;

import java.util.ArrayList;
import java.util.List;

public enum QOption {

    EXECUTE_ALWAYS, WITHOUT_PARAMETERS;

    public static QOption[] from(String value) {
        if (value.contains(",")) {
            String[] options = value.split(",");
            List<QOption> qOptions = new ArrayList<>();
            for (String opt : options) {
                opt = opt.replace("{", "").replace("}", "").replace("QOption.", "");
                qOptions.add(QOption.valueOf(value));
            }
            return qOptions.toArray(new QOption[]{});
        } else {
            value = value.replace("{", "").replace("}", "").replace("QOption.", "");
            return new QOption[]{QOption.valueOf(value)};
        }
    }
}
