
public class ThisAddIn

    Private Sub ThisAddIn_Startup(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Startup
        ' Start of VSTO generated code

        Me.Application = CType(Microsoft.Office.Tools.Excel.ExcelLocale1033Proxy.Wrap(GetType(Excel.Application), Me.Application), Excel.Application)

        ' End of VSTO generated code

    End Sub

    Private Sub ThisAddIn_Shutdown(ByVal sender As Object, ByVal e As System.EventArgs) Handles Me.Shutdown

    End Sub

    Private Sub ThisWorkbook_Open(ByVal Wb As Microsoft.Office.Interop.Excel.Workbook) Handles Application.WorkbookOpen
        Try
            Me.Application.Run("RegisterCallback", New ManagedClass)
            MsgBox("WSIT Endpoint AddIn activated succesfully.")
        Catch ex As Exception
        End Try
    End Sub

End class


<System.Runtime.InteropServices.ComVisible(True)> _
Public Class ManagedClass

    Dim RC As Integer
    Dim ErrMsg As String

    Private Function getWcfClient( _
            ByVal WsitUsername As String, _
            ByVal WsitPassword As String) As localhost.WSITEndpointClient
        Dim wcfClient As New localhost.WSITEndpointClient()
        Dim certLoc As System.Security.Cryptography.X509Certificates.StoreLocation
        certLoc = System.Security.Cryptography.X509Certificates.StoreLocation.LocalMachine
        Dim certNameRoot As System.Security.Cryptography.X509Certificates.StoreName
        certNameRoot = System.Security.Cryptography.X509Certificates.StoreName.Root
        Dim certNameMy As System.Security.Cryptography.X509Certificates.StoreName
        certNameMy = System.Security.Cryptography.X509Certificates.StoreName.My
        Dim certType As System.Security.Cryptography.X509Certificates.X509FindType
        certType = System.Security.Cryptography.X509Certificates.X509FindType.FindBySubjectName
        Dim rcc As System.ServiceModel.Security.X509CertificateRecipientClientCredential = _
        wcfClient.ClientCredentials.ServiceCertificate
        rcc.SetScopedCertificate(certLoc, certNameRoot, certType, "WSSIP", New Uri("http://localhost:8080/sts/SecurityTokenService"))
        wcfClient.ClientCredentials.ServiceCertificate.SetDefaultCertificate(certLoc, certNameRoot, certType, "xwssecurityserver")
        wcfClient.ClientCredentials.UserName.UserName = WsitUsername
        wcfClient.ClientCredentials.UserName.Password = WsitPassword
        Return wcfClient
    End Function

    Public Function getPatientId( _
      ByVal Firstname As String, ByVal Surname As String, _
      ByVal Dob As String, ByVal Ssn As String, _
      ByVal WsitUsername As String, ByVal WsitPassword As String) As Integer
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As Integer = wcfClient.getPatientId(Firstname, Surname, Dob, Ssn)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return 0
        End Try
    End Function

    Public Function getPatientFirstname( _
            ByVal PatientId As Integer, _
            ByVal WsitUsername As String, ByVal WsitPassword As String) As String
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As String = wcfClient.getPatientFirstname(PatientId)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return ""
        End Try
    End Function

    Public Function getPatientSurname( _
            ByVal PatientId As Integer, _
            ByVal WsitUsername As String, ByVal WsitPassword As String) As String
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As String = wcfClient.getPatientSurname(PatientId)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return ""
        End Try
    End Function

    Public Function getPatientDob( _
            ByVal PatientId As Integer, _
            ByVal WsitUsername As String, ByVal WsitPassword As String) As String
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As String = wcfClient.getPatientDOB(PatientId)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return ""
        End Try
    End Function

    Public Function getPatientSsn( _
            ByVal PatientId As Integer, _
            ByVal WsitUsername As String, ByVal WsitPassword As String) As String
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As String = wcfClient.getPatientSSN(PatientId)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return ""
        End Try
    End Function

    Public Function getPatientDiagnosis( _
            ByVal PatientId As Integer, _
            ByVal WsitUsername As String, ByVal WsitPassword As String) As String
        Try
            Dim wcfClient As localhost.WSITEndpointClient = _
                        getWcfClient(WsitUsername, WsitPassword)
            Dim getWsVal As String = wcfClient.getPatientDiagnosis(PatientId)
            Call setRC(0)
            Return getWsVal
        Catch ex As Exception
            Call setRC(1)
            Call setErrMsg(ex.Message)
            Return ""
        End Try
    End Function

    Public Function getRC() As Integer
        Return RC
    End Function

    Private Sub setRC(ByVal newRC As Integer)
        RC = newRC
    End Sub

    Public Function getErrMsg() As String
        Return ErrMsg
    End Function

    Private Sub setErrMsg(ByVal Msg As String)
        ErrMsg = Msg
    End Sub

End Class