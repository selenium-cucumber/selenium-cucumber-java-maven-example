Feature: Verify Calculator functionalities
 
  Scenario: Click on OK button 
    Then I click on element having id "com.android2.calculator3:id/cling_dismiss"

  Scenario: Addition
    Then I click on element having id "com.android2.calculator3:id/digit5"
    Then I click on element having id "com.android2.calculator3:id/plus"
    Then I click on element having id "com.android2.calculator3:id/digit9"
    When I click on element having id "com.android2.calculator3:id/equal"
    Then element having xpath "//android.widget.EditText[@index=0]" should have text as "14"