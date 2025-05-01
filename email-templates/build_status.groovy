<!-- Prepare the build result label -->
<%
    def resultLabel = build.result.toString() == 'SUCCESS' ? '✅ SUCCESS' : '❌ FAILED'
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Build Report: ${project.name} #${build.number} (Status:${resultLabel})</title>
</head>
<body style="font-family: 'Segoe UI', Arial, sans-serif; font-size: 17px; color: #333; line-height: 1.7;">

<!-- Centered Company Logo at the Top -->
<div style="text-align: center; margin-top: 20px; margin-bottom: 10px;">
    <img src="https://www.f3engineers.com/message-logo/F3EnsLogo-350x178.png" alt="F3Engineers Logo" style="height: 60px;">
</div>

<!-- Separator Line -->
<hr style="border: 0; border-top: 2px solid #ddd; margin: 20px auto; width: 80%;">

<!-- Build Report Title and Reminder Text (Centered) -->
<div style="text-align: center; margin-bottom: 20px;">
    <h2 style="margin: 0; font-size: 26px; color: ${build.result.toString() == 'SUCCESS' ? '#28a745' : '#dc3545'};">
        Build Report (${resultLabel})
    </h2>
    <p style="margin: 8px 0 0 0; font-size: 17px; color: #666;">
        This is an automated email from Jenkins. Please do not reply.
    </p>
</div>
<p>
<!-- INFO TABLE -->
<table width="100%" cellpadding="4" cellspacing="0" style="font-size: 16px;">
    <tr><td style="width:150px;"><strong>Project:</strong></td><td>${project.name}</td></tr>
    <tr><td><strong>Build Number:</strong></td><td>#${build.number}</td></tr>
    <tr><td><strong>Result:</strong></td><td style="color: ${build.result.toString() == 'SUCCESS' ? 'green' : 'red'};"><b>${build.result}</b></td></tr>
    <tr><td><strong>Duration:</strong></td><td>${build.durationString}</td></tr>
    <tr><td><strong>URL:</strong></td><td><a href="${rooturl}${build.url}">${rooturl}${build.url}</a></td></tr>
</table>

<!-- GIT COMMIT INFO -->
<h3 style="margin-top: 30px;">🔍 Latest Git Commit</h3>
<%
    def changeSets = build.changeSets
    if (!changeSets.isEmpty()) {
        def allEntries = []
        changeSets.each { cs ->
            cs.items.each { entry ->
                allEntries << entry
            }
        }
        // Sort commits by timestamp DESC (newest first)
        allEntries.sort { -it.timestamp }
    
        if (!allEntries.isEmpty()) {
%>
    <ul style="font-size: 15px;">
<%
            allEntries.each { entry ->
%>
        <li>
            <strong>Author:</strong> ${entry.author} <br/>
            <strong>Date:</strong> ${new Date(entry.timestamp).format("yyyy-MM-dd HH:mm:ss")} <br/>
            <strong>Message:</strong> ${entry.msg}
        </li>
<%
            }
%>
    </ul>
<%
        } else {
%>
    <p style="font-size: 15px;">No commit information available.</p>
<%
        }
    } else {
%>
    <p style="font-size: 15px;">No commit found in this build.</p>
<%
    }
%>

<% if (build.result.toString() != 'SUCCESS') { %>
<!-- LOG (only shown when build is not SUCCESS) -->
<h3 style="margin-top: 30px;">📜 Recent Build Log (Last 100 Lines)</h3>
<pre style="background-color: #f8f9fa; border: 1px solid #ccc; padding: 16px; font-family: 'Courier New', monospace; font-size: 15px; overflow-x: auto;">
<%
    def logLines = build.getLog(100)
    logLines.each { line ->
        out.println(line.replaceAll('<', '&lt;').replaceAll('>', '&gt;'))
    }
%>
</pre>
<% } %>

</body>
</html>