/**
 * validators.js â€” form validation helpers.
 * Skills: JavaScript
 */

export function validateEmail(email) {
    if (!email) return 'Email is required'
    const at = email.indexOf('@')
    if (at < 1 || email.indexOf('.', at) <= at + 1) return 'Invalid email address'
    return null
}

export function validatePassword(password) {
    if (!password) return 'Password is required'
    if (password.length < 8) return 'Password must be at least 8 characters'
    if (!/[A-Z]/.test(password)) return 'Password must contain an uppercase letter'
    if (!/[a-z]/.test(password)) return 'Password must contain a lowercase letter'
    if (!/\d/.test(password)) return 'Password must contain a number'
    return null
}

export function validateUsername(username) {
    if (!username) return 'Username is required'
    if (username.length < 3) return 'Username must be at least 3 characters'
    if (!/^[a-zA-Z0-9_]+$/.test(username)) return 'Only letters, numbers, and underscores allowed'
    return null
}

export function validateRequired(value, fieldName = 'This field') {
    if (!value || (typeof value === 'string' && !value.trim())) {
        return `${fieldName} is required`
    }
    return null
}

export function validateMinLength(value, min, fieldName = 'Field') {
    if (value && value.length < min) return `${fieldName} must be at least ${min} characters`
    return null
}

export function validateMaxLength(value, max, fieldName = 'Field') {
    if (value && value.length > max) return `${fieldName} must not exceed ${max} characters`
    return null
}

/** Returns { valid, errors } for a full registration form. */
export function validateRegisterForm({ fullName, username, email, password }) {
    const errors = {}
    const fn = validateRequired(fullName, 'Full name'); if (fn) errors.fullName = fn
    const un = validateUsername(username); if (un) errors.username = un
    const em = validateEmail(email); if (em) errors.email = em
    const pw = validatePassword(password); if (pw) errors.password = pw
    return { valid: Object.keys(errors).length === 0, errors }
}
