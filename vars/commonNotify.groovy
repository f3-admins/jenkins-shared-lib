// vars/commonNotify.groovy

def call(boolean isSuccess) {
    def status = isSuccess ? "Deploy succeeded" : "Deploy failed"
    def emoji = isSuccess ? "✅" : "❌"

    slackSend(
        channel: '#jenkins',
        message: "${emoji} ${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        tokenCredentialId: 'slack-token'
    )
}