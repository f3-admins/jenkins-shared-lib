// vars/commonNotify.groovy
def call(Map config) {
    // Load the email template file
    def template = libraryResource('emailTemplate.html')

    // Replace placeholder variables in the template with actual values
    def emailBody = template
            .replace('${PROJECT_NAME}', config?.PROJECT_NAME ?: 'Unknown Project')
            .replace('${BUILD_NUMBER}', config?.BUILD_NUMBER?.toString() ?: 'N/A')
            .replace('${BUILD_STATUS}', config?.BUILD_STATUS ?: 'N/A')
            .replace('${CAUSE}', config?.CAUSE ?: 'Unknown')
            .replace('${BUILD_URL}', config?.BUILD_URL ?: '#')
            .replace('${PROJECT_URL}', config?.PROJECT_URL ?: '#')
            .replace('${BUILD_LOG}', config?.BUILD_LOG ?: '')

    // Send the email notification using the configured parameters
    emailext(
        to: config?.to ?: '',
        subject: config?.subject ?: '',
        body: emailBody,
        mimeType: 'text/html'
    )
}