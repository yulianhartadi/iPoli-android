package io.ipoli.android.common

class Validator<in ValueType, out ErrorType>(private val rules: Map<String, PropertyValidator<ValueType, ErrorType>>) {

    companion object {
        operator fun <ValueType, ErrorType> invoke(init: Builder<ValueType, ErrorType>.() -> Unit): Validator<ValueType, ErrorType> {
            return Builder<ValueType, ErrorType>().apply(init).build()
        }
    }

    fun validate(value: ValueType): List<ErrorType> {
        return rules.map {
            it.value.validations
                .filter { it.first.invoke(value) }
                .map { it.second }
        }.flatten()
    }


    class Builder<ValueType, ErrorType> {
        private val childValidations: MutableMap<String, PropertyValidator<ValueType, ErrorType>> = mutableMapOf()

        operator fun String.invoke(init: PropertyValidator<ValueType, ErrorType>.() -> Unit) {
            childValidations.put(this, PropertyValidator<ValueType, ErrorType>().apply(init))
        }

        fun build(): Validator<ValueType, ErrorType> {
            return Validator(childValidations)
        }
    }

    class PropertyValidator<ValueType, ErrorType> {
        internal var validations: MutableList<Pair<ValueType.() -> Boolean, ErrorType>> = mutableListOf()

        fun not(validate: ValueType.() -> Boolean) = validate

        infix fun (ValueType.() -> Boolean).error(error: ErrorType) {
            validations.add(this to error)
        }
    }
}