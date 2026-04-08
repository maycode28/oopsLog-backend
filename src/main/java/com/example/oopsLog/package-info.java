/**
 * Root package architecture rules.
 *
 * <p>Bounded contexts:
 * <ul>
 *   <li>common: shared concerns only (response wrappers, exceptions, cross-cutting).</li>
 *   <li>domain.user: authentication and user profile lifecycle.</li>
 *   <li>domain.analysis: failure input + cognitive analysis output as one feature.</li>
 *   <li>domain.animal: animal catalog and user-animal relationship.</li>
 * </ul>
 *
 * <p>Naming rules:
 * <ul>
 *   <li>Class: PascalCase.</li>
 *   <li>Field/Method: camelCase.</li>
 *   <li>Controller endpoint methods should use use-case names (e.g., signUp, login).</li>
 * </ul>
 *
 * <p>Dependency direction:
 * <ul>
 *   <li>controller -> service -> repository -> entity.</li>
 *   <li>dto is consumed by controller/service, never by entity.</li>
 *   <li>entities must not depend on controller/service/repository packages.</li>
 *   <li>cross-domain direct dependencies are discouraged; use service orchestration.</li>
 *   <li>only common package can be referenced by all domains.</li>
 * </ul>
 */
package com.example.oopsLog;

