/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * LICENSE file that was distributed with this source code.
 */

package org.kitodo.mediaserver.ui.works;

import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kitodo.mediaserver.core.db.entities.Collection;
import org.kitodo.mediaserver.core.db.entities.Work;
import org.kitodo.mediaserver.core.db.repositories.WorkRepository;
import org.kitodo.mediaserver.core.services.WorkService;
import org.kitodo.mediaserver.ui.config.WebMvcConfiguration;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = WorkController.class)
@RunWith(SpringRunner.class)
@Import(WebMvcConfiguration.class)
@AutoConfigureMockMvc
@EnableSpringDataWebSupport // Pageable initialisation

// entityManager Bean and persistence (taken from @DataJpaTest as it conflicts with @SpringBootTest)...
@Transactional
@AutoConfigureCache
@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
public class WorkControllerTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testList() throws Exception {

        String workId1 = "work1";
        String workTitle1 = "Title1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title2";
        String workAllowedNetwork2 = "global";

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.flush();

        assertThat(workRepository.count()).isEqualTo(2);

        mockMvc
            .perform( get("/works") )
            .andExpect( status().isOk() )
            .andExpect( model().attribute("page", hasProperty("content", hasSize(2))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId1)),
                    hasProperty("title", is(workTitle1)),
                    hasProperty("allowedNetwork", is(workAllowedNetwork1))
                )
            ))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId2)),
                    hasProperty("title", is(workTitle2)),
                    hasProperty("allowedNetwork", is(workAllowedNetwork2))
                )
            ))) )
            .andExpect( view().name("works/works") );

        assertThat(workRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSearchWork() throws Exception {

        String workId1 = "work1";
        String workTitle1 = "Title1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title huh";
        String workAllowedNetwork2 = "global";

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.flush();

        assertThat(workRepository.count()).isEqualTo(2);

        mockMvc
            .perform( get("/works").param("search", "huh") )
            .andExpect( status().isOk() )
            .andExpect( model().attribute("page", hasProperty("content", hasSize(1))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId2)),
                    hasProperty("title", is(workTitle2)),
                    hasProperty("allowedNetwork", is(workAllowedNetwork2))
                )
            ))) )
            .andExpect( view().name("works/works") );

        assertThat(workRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchTitleField() throws Exception {

        String workId1 = "huh";
        String workTitle1 = "Title1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title huh";
        String workAllowedNetwork2 = "global";

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.flush();

        assertThat(workRepository.count()).isEqualTo(2);

        mockMvc
            .perform( get("/works").param("search", "title:huh") )
            .andExpect( status().isOk() )
            .andExpect( model().attribute("page", hasProperty("content", hasSize(1))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId2)),
                    hasProperty("title", is(workTitle2)),
                    hasProperty("allowedNetwork", is(workAllowedNetwork2))
                )
            ))) )
            .andExpect( view().name("works/works") );

        assertThat(workRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDisableWorkWithoutReduceMets() throws Exception {

        String workId1 = "work1";
        String workTitle1 = "Title1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title huh";
        String workAllowedNetwork2 = "global";

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.flush();

        assertThat(workRepository.count()).isEqualTo(2);

        mockMvc
            .perform( post("/works")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("workIds", work2.getId())
                .param("action", "set-network")
                .param("params[network]", "disabled")
                .param("params[comment]", "")
                .param("params[reduce]", "")
            )
            .andExpect( status().is3xxRedirection() )
            .andExpect( redirectedUrl("/works") );

        assertThat(work2.getAllowedNetwork()).isEqualTo("disabled");
        assertThat(workRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchWorkWithMultipleCollections() throws Exception {

        String workId1 = "work1";
        String workTitle1 = "Title1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title huh";
        String workAllowedNetwork2 = "global";
        Collection collection1 = new Collection("collection1");
        Collection collection2 = new Collection("collection2");
        Set<Collection> collections1 = new HashSet<>();
        collections1.add(collection1);
        collections1.add(collection2);

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        work1.setCollections(collections1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.flush();

        mockMvc
            .perform( get("/works").param("search", workId1) )
            .andExpect( status().isOk() )
            .andExpect( model().attribute("page", hasProperty("content", hasSize(1))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId1)),
                    hasProperty("title", is(workTitle1))
                )
            ))) )
            .andExpect( view().name("works/works") );

        assertThat(workRepository.count()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void searchWorkWithHostId() throws Exception {

        String workId1 = "work1";
        String workTitle1 = "Title1";
        String workHostId1 = "HostId1";
        String workAllowedNetwork1 = "global";
        String workId2 = "work2";
        String workTitle2 = "Title huh";
        String workAllowedNetwork2 = "global";
        String workTitle3 = "Title hah";
        String workAllowedNetwork3 = "global";

        Work work1 = new Work(workId1, workTitle1);
        work1.setAllowedNetwork(workAllowedNetwork1);
        work1.setHostId(workHostId1);
        Work work2 = new Work(workId2, workTitle2);
        work2.setAllowedNetwork(workAllowedNetwork2);
        Work work3 = new Work(workHostId1, workTitle3);
        work3.setAllowedNetwork(workAllowedNetwork3);
        entityManager.persist(work1);
        entityManager.persist(work2);
        entityManager.persist(work3);
        entityManager.flush();

        mockMvc
            .perform( get("/works").param("search", "hostId:" + workHostId1) )
            .andExpect( status().isOk() )
            .andExpect( model().attribute("page", hasProperty("content", hasSize(1))) )
            .andExpect( model().attribute("page", hasProperty("content", hasItem(
                allOf(
                    hasProperty("id", is(workId1)),
                    hasProperty("title", is(workTitle1))
                )
            ))) )
            .andExpect( view().name("works/works") );

        assertThat(workRepository.count()).isEqualTo(3);
    }

}
