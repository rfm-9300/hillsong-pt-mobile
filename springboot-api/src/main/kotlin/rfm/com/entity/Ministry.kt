package rfm.com.entity

/**
 * Ministry / connection group category.
 * Stored as strings in MongoDB (not ordinals) so reordering never breaks data.
 */
enum class Ministry {
    SISTERHOOD,
    JOVENS_YXYA,
    MENS,
    CASAIS,
    THIRTY_PLUS,
    GERAL
}
