package fr.istic.service;

import io.quarkus.panache.common.Page;
import fr.istic.domain.Exam;
import fr.istic.domain.Question;
import fr.istic.domain.Zone;
import fr.istic.service.customdto.ResizeZoneDTO;
import fr.istic.service.dto.ZoneDTO;
import fr.istic.service.mapper.ZoneMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class ZoneService {

    private final Logger log = LoggerFactory.getLogger(ZoneService.class);

    @Inject
    ZoneMapper zoneMapper;

    @Transactional
    public ZoneDTO persistOrUpdate(ZoneDTO zoneDTO) {
        log.debug("Request to save Zone : {}", zoneDTO);
        var zone = zoneMapper.toEntity(zoneDTO);
        zone = Zone.persistOrUpdate(zone);
        return zoneMapper.toDto(zone);
    }




    @Transactional
    void deleteZone (long id){
        Zone.deleteById(id);
    }


    /**
     * Delete the Zone by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Zone : {}", id);
        Question.findQuestionbyZoneId(id).firstResultOptional().ifPresentOrElse(q -> {
            q.delete();
        }, new Runnable() {
            public void run() {
                Exam.findExamThatMatchZoneId(id).firstResultOptional().ifPresentOrElse(exam-> {
                    if (exam.namezone != null && exam.namezone.id.equals(id)){
                        Exam.removeNameZoneId(exam);
                    }
                    else if (exam.firstnamezone != null &&  exam.firstnamezone.id.equals(id)){
                        Exam.removeFirstNameZoneId(exam);

                    }
                    else if (exam.idzone != null && exam.idzone.id.equals(id)){
                        Exam.removeIdZoneId(exam);
                    }
                    else if (exam.notezone != null && exam.notezone.id.equals(id))
                    {
                        Exam.removeNoteZoneId(exam);
                    }
                    ZoneService.this.deleteZone(id);
                },  new Runnable() {

                    public void run() {
                        ZoneService.this.deleteZone(id);
                    }
                }
                );


            }
        });
    }

    /**
     * Get one zone by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    public Optional<ZoneDTO> findOne(Long id) {
        log.debug("Request to get Zone : {}", id);
        return Zone.findByIdOptional(id)
            .map(zone -> zoneMapper.toDto((Zone) zone));
    }

    /**
     * Get all the zones.
     * @param page the pagination information.
     * @return the list of entities.
     */
    public Paged<ZoneDTO> findAll(Page page) {
        log.debug("Request to get all Zones");
        return new Paged<>(Zone.findAll().page(page))
            .map(zone -> zoneMapper.toDto((Zone) zone));
    }


        /**
     * Partially update a zone.
     *
     * @param zoneDTO the entity to update partially.
     * @return the persisted entity.
     */
    @Transactional
    public Optional<ZoneDTO> partialUpdate(ZoneDTO zoneDTO) {
        log.debug("Request to partially update Zone : {}", zoneDTO);
        Zone existingZone = Zone.findById(zoneDTO.id);

        zoneMapper.partialUpdate(existingZone, zoneDTO);
        Zone z1 = Zone.update(existingZone);
        return Optional.of(zoneMapper.toDto(z1));
    }

    @Transactional
    public Optional<ZoneDTO> partialResizeUpdate(ResizeZoneDTO rzoneDTO, long id) {
        log.debug("Request to partially update Zone : {}", rzoneDTO);
        Zone existingZone = Zone.findById(id);
        existingZone.xInit = rzoneDTO.getLeft();
        existingZone.yInit = rzoneDTO.getTop();
        existingZone.height = (int) Math.round(rzoneDTO.getY() * existingZone.height);
        existingZone.width = (int) Math.round(rzoneDTO.getX() * existingZone.width);
        Zone z1 = Zone.update(existingZone);
        return Optional.of(zoneMapper.toDto(z1));
    }


}
