package ${groupId}.api.validation;

import jakarta.validation.groups.Default;

public interface ValidationGroups {
    interface PERSIST extends Default {
    }

    interface UPDATE extends Default {
    }

    interface DELETE extends Default {
    }
}
