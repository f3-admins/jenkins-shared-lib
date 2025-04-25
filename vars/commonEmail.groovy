// vars/commonEmail.groovy

def call(Map params) {
    if (!params?.to) {
        error "Email recipient ('to') is required."
    }
    if (params?.status == null) { // Check for null explicitly since status is boolean
        error "Build status ('status') is required."
    }
    if (!params?.job_name) {
        error "Job name ('job_name') is required."
    }
    if (!params?.build_number) {
        error "Build number ('build_number') is required."
    }

    // Determine status message based on the boolean value
    def statusMessage = params.status ? "✅ SUCCESS" : "❌ FAILURE"

    def subject = "${statusMessage}: ${params.job_name} #${params.build_number} Build Report"
    
    emailext(
        to: params.to,
        subject: subject,
        mimeType: 'text/html',
        body: '${SCRIPT, template="build_status.groovy"}'
        //body: '${SCRIPT, template="jenkins-generic-matrix-email-html.template"}'
    )
}