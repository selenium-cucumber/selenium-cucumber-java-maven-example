Feature: Login
        As a user I should able to login into my app
 
 Scenario: I login with valid credential
 		Given I navigate to "http://the-internet.herokuapp.com/login"
        And I enter "tomsmith" into input field having id "username"
        And I enter "SuperSecretPassword!" into input field having id "password"
        When I click on element having class "radius"
        Then I should get logged-in
 
 Scenario: Close browser
 		Then I close browser 