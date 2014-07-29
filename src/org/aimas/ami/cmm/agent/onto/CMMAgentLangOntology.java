// file: CMMAgentLangOntology.java generated by ontology bean generator.  DO NOT EDIT, UNLESS YOU ARE REALLY SURE WHAT YOU ARE DOING!
package org.aimas.ami.cmm.agent.onto;

import jade.content.onto.*;
import jade.content.schema.*;

/** file: CMMAgentLangOntology.java
 * @author OntologyBeanGenerator v4.1
 * @version 2014/07/29, 17:38:36
 */
public class CMMAgentLangOntology extends jade.content.onto.Ontology  {

  private static final long serialVersionUID = 7138998227437283033L;

  //NAME
  public static final String ONTOLOGY_NAME = "CMMAgentLang";
  // The singleton instance of this ontology
  private static Ontology theInstance = new CMMAgentLangOntology();
  public static Ontology getInstance() {
     return theInstance;
  }


   // VOCABULARY
    public static final String USERQUERY_REPEATINTERVAL="repeatInterval";
    public static final String USERQUERY_DOMAIN_UPPER_BOUND="domain_upper_bound";
    public static final String USERQUERY_QUERYCONTENT="queryContent";
    public static final String USERQUERY_DOMAIN_LOWER_BOUND="domain_lower_bound";
    public static final String USERQUERY_QUERYTARGET="queryTarget";
    public static final String USERQUERY="UserQuery";
    public static final String DOMAINDETECTED_DOMAIN="domain";
    public static final String DOMAINDETECTED="DomainDetected";
    public static final String SENSORPRESENT_AGENT="agent";
    public static final String SENSORPRESENT="SensorPresent";
    public static final String ASSERTIONUPDATED_ASSERTIONCONTENT="assertionContent";
    public static final String ASSERTIONUPDATED_ASSERTION="assertion";
    public static final String ASSERTIONUPDATED="AssertionUpdated";
    public static final String QUERYHANDLERPRESENT_AGENT="agent";
    public static final String QUERYHANDLERPRESENT_ISPRIMARY="isPrimary";
    public static final String QUERYHANDLERPRESENT="QueryHandlerPresent";
    public static final String ASSERTIONDISTRIBUTION_ASSIGNMENT="assignment";
    public static final String ASSERTIONDISTRIBUTION="AssertionDistribution";
    public static final String QUERYBASE_QUERYHANDLER="queryHandler";
    public static final String QUERYBASE="QueryBase";
    public static final String DOMAINDESCRIPTION_DOMAIN="domain";
    public static final String DOMAINDESCRIPTION="DomainDescription";
    public static final String ENABLEASSERTIONS_CAPABILITY="capability";
    public static final String ENABLEASSERTIONS="EnableAssertions";
    public static final String STOPSENDING="StopSending";
    public static final String RESOLVEQUERYBASE_FORQUERY="forQuery";
    public static final String RESOLVEQUERYBASE="ResolveQueryBase";
    public static final String EXECTASK_ASSERTION="assertion";
    public static final String EXECTASK="ExecTask";
    public static final String REGISTERUSER_USER="user";
    public static final String REGISTERUSER="RegisterUser";
    public static final String SETUPDATEMODE_UPDATERATE="updateRate";
    public static final String SETUPDATEMODE_UPDATEMODE="updateMode";
    public static final String SETUPDATEMODE="SetUpdateMode";
    public static final String INFORMDOMAIN_APPIDENTIFIER="appIdentifier";
    public static final String INFORMDOMAIN="InformDomain";
    public static final String APPLYCONFIGURATION_CONFIGCONTENT="configContent";
    public static final String APPLYCONFIGURATION="ApplyConfiguration";
    public static final String STARTSENDING="StartSending";
    public static final String INFORMASSERTIONS="InformAssertions";
    public static final String COLLECTQUERYBASE="CollectQueryBase";
    public static final String CONNECTTODOMAIN_DOMAIN="domain";
    public static final String CONNECTTODOMAIN="ConnectToDomain";
    public static final String PUBLISHASSERTIONS_CAPABILITY="capability";
    public static final String PUBLISHASSERTIONS="PublishAssertions";
    public static final String CONTEXTDOMAIN_DOMAINDIMENSION="domainDimension";
    public static final String CONTEXTDOMAIN_DOMAINENTITY="domainEntity";
    public static final String CONTEXTDOMAIN_DOMAINVALUE="domainValue";
    public static final String CONTEXTDOMAIN="ContextDomain";
    public static final String ASSERTIONDESCRIPTION_ANNOTATIONTYPE="annotationType";
    public static final String ASSERTIONDESCRIPTION_ASSERTIONTYPE="assertionType";
    public static final String ASSERTIONDESCRIPTION="AssertionDescription";
    public static final String ASSERTIONASSIGNMENT_CAPABILITY="capability";
    public static final String ASSERTIONASSIGNMENT_COORDINATOR="coordinator";
    public static final String ASSERTIONASSIGNMENT="AssertionAssignment";

  /**
   * Constructor
  */
  private CMMAgentLangOntology(){ 
    super(ONTOLOGY_NAME, BasicOntology.getInstance());
    try { 

    // adding Concept(s)
    ConceptSchema assertionAssignmentSchema = new ConceptSchema(ASSERTIONASSIGNMENT);
    add(assertionAssignmentSchema, org.aimas.ami.cmm.agent.onto.AssertionAssignment.class);
    ConceptSchema assertionDescriptionSchema = new ConceptSchema(ASSERTIONDESCRIPTION);
    add(assertionDescriptionSchema, org.aimas.ami.cmm.agent.onto.AssertionDescription.class);
    ConceptSchema contextDomainSchema = new ConceptSchema(CONTEXTDOMAIN);
    add(contextDomainSchema, org.aimas.ami.cmm.agent.onto.ContextDomain.class);

    // adding AgentAction(s)
    AgentActionSchema publishAssertionsSchema = new AgentActionSchema(PUBLISHASSERTIONS);
    add(publishAssertionsSchema, org.aimas.ami.cmm.agent.onto.PublishAssertions.class);
    AgentActionSchema connectToDomainSchema = new AgentActionSchema(CONNECTTODOMAIN);
    add(connectToDomainSchema, org.aimas.ami.cmm.agent.onto.ConnectToDomain.class);
    AgentActionSchema collectQueryBaseSchema = new AgentActionSchema(COLLECTQUERYBASE);
    add(collectQueryBaseSchema, org.aimas.ami.cmm.agent.onto.CollectQueryBase.class);
    AgentActionSchema informAssertionsSchema = new AgentActionSchema(INFORMASSERTIONS);
    add(informAssertionsSchema, org.aimas.ami.cmm.agent.onto.InformAssertions.class);
    AgentActionSchema startSendingSchema = new AgentActionSchema(STARTSENDING);
    add(startSendingSchema, org.aimas.ami.cmm.agent.onto.StartSending.class);
    AgentActionSchema applyConfigurationSchema = new AgentActionSchema(APPLYCONFIGURATION);
    add(applyConfigurationSchema, org.aimas.ami.cmm.agent.onto.ApplyConfiguration.class);
    AgentActionSchema informDomainSchema = new AgentActionSchema(INFORMDOMAIN);
    add(informDomainSchema, org.aimas.ami.cmm.agent.onto.InformDomain.class);
    AgentActionSchema setUpdateModeSchema = new AgentActionSchema(SETUPDATEMODE);
    add(setUpdateModeSchema, org.aimas.ami.cmm.agent.onto.SetUpdateMode.class);
    AgentActionSchema registerUserSchema = new AgentActionSchema(REGISTERUSER);
    add(registerUserSchema, org.aimas.ami.cmm.agent.onto.RegisterUser.class);
    AgentActionSchema execTaskSchema = new AgentActionSchema(EXECTASK);
    add(execTaskSchema, org.aimas.ami.cmm.agent.onto.ExecTask.class);
    AgentActionSchema resolveQueryBaseSchema = new AgentActionSchema(RESOLVEQUERYBASE);
    add(resolveQueryBaseSchema, org.aimas.ami.cmm.agent.onto.ResolveQueryBase.class);
    AgentActionSchema stopSendingSchema = new AgentActionSchema(STOPSENDING);
    add(stopSendingSchema, org.aimas.ami.cmm.agent.onto.StopSending.class);
    AgentActionSchema enableAssertionsSchema = new AgentActionSchema(ENABLEASSERTIONS);
    add(enableAssertionsSchema, org.aimas.ami.cmm.agent.onto.EnableAssertions.class);

    // adding AID(s)

    // adding Predicate(s)
    PredicateSchema domainDescriptionSchema = new PredicateSchema(DOMAINDESCRIPTION);
    add(domainDescriptionSchema, org.aimas.ami.cmm.agent.onto.DomainDescription.class);
    PredicateSchema queryBaseSchema = new PredicateSchema(QUERYBASE);
    add(queryBaseSchema, org.aimas.ami.cmm.agent.onto.QueryBase.class);
    PredicateSchema assertionDistributionSchema = new PredicateSchema(ASSERTIONDISTRIBUTION);
    add(assertionDistributionSchema, org.aimas.ami.cmm.agent.onto.AssertionDistribution.class);
    PredicateSchema queryHandlerPresentSchema = new PredicateSchema(QUERYHANDLERPRESENT);
    add(queryHandlerPresentSchema, org.aimas.ami.cmm.agent.onto.QueryHandlerPresent.class);
    PredicateSchema assertionUpdatedSchema = new PredicateSchema(ASSERTIONUPDATED);
    add(assertionUpdatedSchema, org.aimas.ami.cmm.agent.onto.AssertionUpdated.class);
    PredicateSchema sensorPresentSchema = new PredicateSchema(SENSORPRESENT);
    add(sensorPresentSchema, org.aimas.ami.cmm.agent.onto.SensorPresent.class);
    PredicateSchema domainDetectedSchema = new PredicateSchema(DOMAINDETECTED);
    add(domainDetectedSchema, org.aimas.ami.cmm.agent.onto.DomainDetected.class);
    PredicateSchema userQuerySchema = new PredicateSchema(USERQUERY);
    add(userQuerySchema, org.aimas.ami.cmm.agent.onto.UserQuery.class);


    // adding fields
    assertionAssignmentSchema.add(ASSERTIONASSIGNMENT_COORDINATOR, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    assertionAssignmentSchema.add(ASSERTIONASSIGNMENT_CAPABILITY, assertionDescriptionSchema, 1, ObjectSchema.UNLIMITED);
    assertionDescriptionSchema.add(ASSERTIONDESCRIPTION_ASSERTIONTYPE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    assertionDescriptionSchema.add(ASSERTIONDESCRIPTION_ANNOTATIONTYPE, (TermSchema)getSchema(BasicOntology.STRING), 0, ObjectSchema.UNLIMITED);
    contextDomainSchema.add(CONTEXTDOMAIN_DOMAINVALUE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    contextDomainSchema.add(CONTEXTDOMAIN_DOMAINENTITY, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    contextDomainSchema.add(CONTEXTDOMAIN_DOMAINDIMENSION, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    publishAssertionsSchema.add(PUBLISHASSERTIONS_CAPABILITY, assertionDescriptionSchema, 1, ObjectSchema.UNLIMITED);
    connectToDomainSchema.add(CONNECTTODOMAIN_DOMAIN, contextDomainSchema, ObjectSchema.MANDATORY);
    applyConfigurationSchema.add(APPLYCONFIGURATION_CONFIGCONTENT, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    informDomainSchema.add(INFORMDOMAIN_APPIDENTIFIER, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    setUpdateModeSchema.add(SETUPDATEMODE_UPDATEMODE, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    setUpdateModeSchema.add(SETUPDATEMODE_UPDATERATE, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    registerUserSchema.add(REGISTERUSER_USER, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    execTaskSchema.add(EXECTASK_ASSERTION, assertionDescriptionSchema, ObjectSchema.MANDATORY);
    resolveQueryBaseSchema.add(RESOLVEQUERYBASE_FORQUERY, userQuerySchema, ObjectSchema.MANDATORY);
    enableAssertionsSchema.add(ENABLEASSERTIONS_CAPABILITY, assertionDescriptionSchema, 1, ObjectSchema.UNLIMITED);
    domainDescriptionSchema.add(DOMAINDESCRIPTION_DOMAIN, contextDomainSchema, ObjectSchema.MANDATORY);
    queryBaseSchema.add(QUERYBASE_QUERYHANDLER, (ConceptSchema)getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);
    assertionDistributionSchema.add(ASSERTIONDISTRIBUTION_ASSIGNMENT, assertionAssignmentSchema, 1, ObjectSchema.UNLIMITED);
    queryHandlerPresentSchema.add(QUERYHANDLERPRESENT_ISPRIMARY, (TermSchema)getSchema(BasicOntology.BOOLEAN), ObjectSchema.MANDATORY);
    queryHandlerPresentSchema.add(QUERYHANDLERPRESENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    assertionUpdatedSchema.add(ASSERTIONUPDATED_ASSERTION, assertionDescriptionSchema, ObjectSchema.MANDATORY);
    assertionUpdatedSchema.add(ASSERTIONUPDATED_ASSERTIONCONTENT, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    sensorPresentSchema.add(SENSORPRESENT_AGENT, (ConceptSchema)getSchema(BasicOntology.AID), ObjectSchema.MANDATORY);
    domainDetectedSchema.add(DOMAINDETECTED_DOMAIN, contextDomainSchema, ObjectSchema.MANDATORY);
    userQuerySchema.add(USERQUERY_QUERYTARGET, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    userQuerySchema.add(USERQUERY_DOMAIN_LOWER_BOUND, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    userQuerySchema.add(USERQUERY_QUERYCONTENT, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.MANDATORY);
    userQuerySchema.add(USERQUERY_DOMAIN_UPPER_BOUND, (TermSchema)getSchema(BasicOntology.STRING), ObjectSchema.OPTIONAL);
    userQuerySchema.add(USERQUERY_REPEATINTERVAL, (TermSchema)getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);

    // adding name mappings

    // adding inheritance
    startSendingSchema.addSuperSchema(execTaskSchema);
    setUpdateModeSchema.addSuperSchema(execTaskSchema);
    stopSendingSchema.addSuperSchema(execTaskSchema);

   }catch (java.lang.Exception e) {e.printStackTrace();}
  }
}
