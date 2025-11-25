// vars/awsCodeArtifactLogin.groovy
// A Jenkins Shared Library step to configure AWS CodeArtifact NuGet credential provider
// and ensure the correct NuGet source is present.

import groovy.json.JsonOutput

def call(Map params = [:]) {
    // Resolve inputs from params or env
    def AWS_REGION = params.AWS_REGION ?: (env.AWS_REGION ?: '')
    def DOMAIN_NAME = params.DOMAIN_NAME ?: (env.DOMAIN_NAME ?: '')
    def DOMAIN_OWNER = params.DOMAIN_OWNER ?: (env.DOMAIN_OWNER ?: '')
    def REPO_NAME = params.REPO_NAME ?: (env.REPO_NAME ?: '')
    def EXPECTED_SOURCE_NAME = params.EXPECTED_SOURCE_NAME ?: (env.EXPECTED_SOURCE_NAME ?: '')

    if (!AWS_REGION) error "AWS_REGION is required"
    if (!DOMAIN_NAME) error "DOMAIN_NAME is required"
    if (!DOMAIN_OWNER) error "DOMAIN_OWNER is required"
    if (!REPO_NAME) error "REPO_NAME is required"

    def CODEARTIFACT_URL = params.CODEARTIFACT_URL ?: (env.CODEARTIFACT_URL ?: "https://${DOMAIN_NAME}-${DOMAIN_OWNER}.d.codeartifact.${AWS_REGION}.amazonaws.com/nuget/${REPO_NAME}/")
    if (!EXPECTED_SOURCE_NAME) {
        EXPECTED_SOURCE_NAME = params.DEFAULT_SOURCE_NAME ?: "${DOMAIN_NAME}/${REPO_NAME}"
    }

    // Ensure messages appear in build log
    echo "Configuring AWS CodeArtifact for NuGet..."
    echo "AWS_REGION=${AWS_REGION}, DOMAIN=${DOMAIN_NAME}, OWNER=${DOMAIN_OWNER}, REPO=${REPO_NAME}"
    echo "CODEARTIFACT_URL=${CODEARTIFACT_URL}"
    echo "EXPECTED_SOURCE_NAME=${EXPECTED_SOURCE_NAME}"

    withAWS(region: AWS_REGION) {
        // Ensure credential provider is installed
        echo "Ensuring AWS CodeArtifact Credential Provider is installed..."
        // Use '|| true' to avoid failing the step if update or install command returns non-zero unexpectedly
        sh label: 'Install/Update CodeArtifact credential provider', script: '''
            set -eu
            dotnet tool update -g AWS.CodeArtifact.NuGet.CredentialProvider || \
            dotnet tool install -g AWS.CodeArtifact.NuGet.CredentialProvider || true
        '''

        echo "Copy the credential provider to the NuGet plugins"
        sh label: 'Install credential provider plugin', script: '''
            set -eu
            export PATH="$PATH:$HOME/.dotnet/tools"
            dotnet codeartifact-creds install
        '''

        echo "Ensuring NuGet source is configured..."

        // List existing sources
        def listOutput = sh(script: """
            set -e
            export PATH=\"$PATH:$HOME/.dotnet/tools\"
            dotnet nuget list source
        """, returnStdout: true).trim()

        // Parse to find any existing source that points to our CODEARTIFACT_URL
        // dotnet output usually alternates: Name: <name> then   Source: <url>
        def lines = listOutput.readLines()
        String matchedName = null
        for (int i = 0; i < lines.size(); i++) {
            def line = lines[i]
            def next = (i + 1 < lines.size()) ? lines[i + 1] : null
            if (next && next.contains(CODEARTIFACT_URL)) {
                // try to parse name from current line
                if (line.toLowerCase().startsWith('name:')) {
                    matchedName = line.substring(line.indexOf(':') + 1).trim()
                } else {
                    def parts = line.trim().split(/\s+/)
                    matchedName = parts.size() >= 2 ? parts[1] : line.trim()
                }
                break
            }
        }

        if (matchedName && matchedName != EXPECTED_SOURCE_NAME) {
            echo "Removing conflicting source '${matchedName}'..."
            sh "dotnet nuget remove source '${matchedName}' || true"
        }

        // Refresh list after possible removal
        listOutput = sh(script: "dotnet nuget list source", returnStdout: true).trim()
        boolean correctSourceExists = listOutput.contains("Name: ${EXPECTED_SOURCE_NAME}")

        if (!correctSourceExists) {
            echo "Adding CodeArtifact NuGet source '${EXPECTED_SOURCE_NAME}'..."
            sh label: 'Add NuGet source', script: """
                set -e
                dotnet nuget add source '${CODEARTIFACT_URL}' \
                    --name '${EXPECTED_SOURCE_NAME}'
            """
        } else {
            echo "Source '${EXPECTED_SOURCE_NAME}' already exists."
        }

        echo "Ready to restore with credential provider."

        def resultJson = JsonOutput.toJson([
            NUGET_SOURCE_NAME: EXPECTED_SOURCE_NAME
        ])
        currentBuild.description = resultJson

        // Return a map so callers can consume it programmatically
        return [
            NUGET_SOURCE_NAME: EXPECTED_SOURCE_NAME,
            CODEARTIFACT_URL: CODEARTIFACT_URL
        ]
    }
}
