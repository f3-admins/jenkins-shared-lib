// vars/commonEmail.groovy

def call(Map config) {
    if (!config?.to) {
        error "Email recipient ('to') is required."
    }
    if (!config?.subject) {
        error "Email subject is required."
    }

    emailext(
        to: config.to,
        subject: config.subject,
        mimeType: 'text/html',
        body: '${SCRIPT, template="build_status.groovy"}'
    )
}