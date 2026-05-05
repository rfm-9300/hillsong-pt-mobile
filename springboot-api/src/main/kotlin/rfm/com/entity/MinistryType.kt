package rfm.com.entity

/**
 * Classification tag for ministries. Used both as a "category" filter on
 * connection groups (Sisterhood, Casais, etc.) and on full Ministry
 * documents (Worship, Kids, Ushers, etc.).
 *
 * Stored as strings in MongoDB (not ordinals) so reordering never breaks data.
 */
enum class MinistryType {
    // Connection-group categories (existing)
    SISTERHOOD,
    JOVENS_YXYA,
    MENS,
    CASAIS,
    THIRTY_PLUS,
    GERAL,

    // Service-team categories (new)
    WORSHIP,
    KIDS,
    USHERS,
    HOSPITALITY,
    MEDIA,
    PRODUCTION,
    CONNECT,
    CAFE
}
